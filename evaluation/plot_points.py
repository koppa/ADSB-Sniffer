#!/usr/bin/python

USAGE = """
./plot_points.py SQLITE_FILE

Parse a recording of the ADS-B Sniffer Android application and export its data
to a kml file, which can be read by f. e. Google Earth.

Requirements: python2, simplekml and numpy


"""


import sqlite3
import simplekml
import sys
import numpy as np
import sys
from datetime import datetime
from __future__ import print_function

from adsb import decoder, cpr

cpr = cpr.cpr_decoder()


def main(dbname):
    conn = sqlite3.connect(dbname)
    cursor = conn.cursor()

    kml = simplekml.Kml()

    # cursor.execute('SELECT longitude, latitude FROM position')
    # coords = cursor.fetchall()

    # line = kml.newlinestring(name='Spur',
    #                          description='bla <br /> bla',
    #                          coords=coords,
    #                          #altitudemode='absolute',
    #                          #timespan=options.time and
    #                          #        simplekml.TimeSpan(begin=start, end=stop)
    #                          )
    cursor.execute('SELECT latitude, longitude FROM position')
    # position = (46.79848,8.231773,1000) # center of Switzerland
    position = cursor.fetchone()
    if not position:
        #position = (46.79848,8.231773,1000) # center of Switzerland
         position = (49.444722, 7.768889)



    cursor.execute('SELECT flight, message FROM messages')

    result = cursor.fetchall()
    # group by flight
    grouped = [(flight, [m for f,m in result if f == flight]) for flight in set(zip(*result)[0])]


    for f, ms in grouped:
        positions = []
        for message in ms:
            msg = long(message[8:22], 16)
            format = (msg >> 51) & 0x1F

            if format >= 9 and format <= 18 or \
               format >= 20 and format <= 22:


                icao24 = "abc"
                dev = position
                received = 0


                try:
                    (surveillance_status, nic_suppl, alttype, altitude, time, cpr_format, decoded_lat, decoded_lon) = decoder.decodePosition(icao24, msg, dev, cpr, received)

                    if cpr_format == 1:
                        # check if valid
                        if decoded_lat > 50:
                            sys.stderr.write ("cpr fehler")
                            #continue

                    # print decoded_lat, decoded_lon
                    positions.append((decoded_lon, decoded_lat))
                except Exception as e:
                    continue


        if positions:

            cursor.execute('SELECT first,last,icao24 FROM flights WHERE id={}'.format(f))
            (first,last,icao24) = cursor.fetchone()
            date_first = datetime.fromtimestamp(int(first))
            try:
                date_last = datetime.fromtimestamp(int(last))
            except:
                date_last = ""
            ls = kml.newlinestring(name='Flug {}'.format(f),
                         description='ICAO24: {}<br />Nachrichten empfangen: {}<br />Erste Nachricht: {}<br />Letzte Nachricht: {}'.format(icao24, len(ms), date_first, date_last),
                         coords=positions,
                         #altitudemode='clampToGround',
                         #timespan=options.time and
                         #        simplekml.TimeSpan(begin=start, end=stop)
                         )
            ls.style.linestyle.width = 2
            ls.style.linestyle.color = simplekml.Color.red

    print(kml.kml())

if __name__ == '__main__':
    if len(sys.argv) == 2:
        main(sys.argv[1])
    else:
        print(USAGE)

