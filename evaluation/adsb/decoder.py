import time, os, sys, math
from string import split, join
from altitude import decode_alt
import cpr
import exceptions

def charmap(d):
    if d > 0 and d < 27:
        retval = chr(ord("A")+d-1)
    elif d == 32:
        retval = " "
    elif d > 47 and d < 58:
        retval = chr(ord("0")+d-48)
    else:
        retval = " "

    return retval
    
def decodeHeader(header):
    icao24 = header & 0xFFFFFF
    capability = (header >> 24) & 0x7
    
    return [icao24, capability]

def decodeIdentification(msg):
    frmt = (msg >> 51) & 0x1F
    catset = ['A', 'B', 'C', 'D'][4-frmt]
    emitter_category = (msg >> 48) & 0x7 # see Doc 9871 Table A-2-8

    callsign = ""
    for i in range(0, 8):
        callsign += charmap( msg >> (42-6*i) & 0x3F)
    
    return [catset, emitter_category, callsign]

def parseAirbornePosition(icao24, msg, receiver_pos, decoder, ts):
    surveillance_status = (msg >> 49) & 0x3
    nic_suppl = bool((msg >> 48) & 1)
    enc_alt = (msg >> 36) & 0x0FFF
    time = bool((msg >> 35) & 1)
    cpr_format = (msg >> 34) & 1
    encoded_lat = (msg >> 17) & 0x1FFFF
    encoded_lon = msg & 0x1FFFF
    
    if encoded_lat == 0 or encoded_lon == 0:
        raise cpr.CPRNoPositionError
    
    altitude = decode_alt(enc_alt, False)
    
    decoder.set_location(receiver_pos)
    [decoded_lat, decoded_lon] = decoder.decode(icao24, encoded_lat, encoded_lon, cpr_format, ts, False)

    return [surveillance_status, nic_suppl, altitude, time, cpr_format, decoded_lat, decoded_lon]

def decodePosition(icao24, msg, receiver_pos, decoder, ts):
    frmt = (msg >> 51) & 0x1F
    # position
    [surveillance_status, nic_suppl, altitude, time, cpr_format, decoded_lat, decoded_lon] = \
        parseAirbornePosition(icao24, msg, receiver_pos, decoder, ts)
    
    alttype = "Baro" if frmt >= 9 and frmt <= 18 else "GNSS"
    
    return [surveillance_status, nic_suppl, alttype, altitude, time, cpr_format, decoded_lat, decoded_lon]

def decodeVelocity(msg):
    alt_geo_diff = msg & 0x7F - 1
    above_below = bool((msg >> 7) & 1)
    if above_below:
        alt_geo_diff = 0 - alt_geo_diff;
    vert_spd = float((msg >> 10) & 0x1FF - 1)
    ud = bool((msg >> 19) & 1)
    if ud:
        vert_spd = 0 - vert_spd
    baro = bool((msg >> 20) & 1)
    ns_vel = float((msg >> 21) & 0x3FF - 1)
    ns = bool((msg >> 31) & 1)
    ew_vel = float((msg >> 32) & 0x3FF - 1)
    ew = bool((msg >> 42) & 1)
    subtype = (msg >> 48) & 0x07

    if subtype == 0x02:
        ns_vel *= 4
        ew_vel *= 4

    vert_spd *= 64
    alt_geo_diff *= 25
    
    velocity = math.hypot(ns_vel, ew_vel)
    if ew:
        ew_vel = 0 - ew_vel
    
    if ns_vel == 0:
        heading = 0
    else:
        heading = math.atan(float(ew_vel) / float(ns_vel)) * (180.0 / math.pi)
    if ns:
        heading = 180 - heading
    if heading < 0:
        heading += 360
        
    nac = (msg >> 43) & 7
    adsb_conflict_detection = bool((msg >> 46) & 1)
    intent_change = bool((msg >> 47) & 1)
    supersonic = (subtype == 2)

    return [supersonic, intent_change, adsb_conflict_detection, nac, velocity, heading, baro, vert_spd, alt_geo_diff]
    
def decodeStatus(msg):
    subtype = (msg >> 48) & 0x7
    emergency = (msg >> 45) & 0x7
    reserved = msg & 0x1FFFFFFFFFFF
        
    return [subtype, emergency, reserved]
    
def decodeOpStatus(msg):
    version = (msg >> 13) & 0x7
    nic_suppl = (msg >> 12) & 1
    nac_pos = (msg >> 8) & 0xF
    sil = (msg >> 4) & 0x3
    magnetic_north = (msg >> 2) & 1
    reserved = msg & 3
    
    # airborne status message
    capability = (msg >> 32) & 0xFFFF
    opmode = (msg >> 16) & 0xFFFF
    gva = (msg >> 6) & 0x3
    nic_baro = (msg >> 3) & 1

    return [capability, opmode, version, nic_suppl, nac_pos, gva, sil, nic_baro, magnetic_north, reserved]
        
