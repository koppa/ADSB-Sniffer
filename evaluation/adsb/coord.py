# This file is part of the mode-s suite for gnuradio
#
# The mode-s suite contains an implementation for
# the physical modulation of mode-s using gnuradio
# and a couple of bindings and mode-s and aviation
# specific higher level implementations in python.
#
# Author:   Matthias Schaefer (armasuisse)
# Email:    m_schae3[at]cs.uni-kl.de
#

"""
Some general function to handle coordinates
"""

from math import asin, atan, atan2, sin, cos, acos
from math import radians, degrees
from math import sqrt

def spherical2cartesian(coord, home=None):
    lat, lon, alt = coord
    
    # check range
    assert(abs(lat) <= 90), "Latitude out of range."
    assert(abs(lon) <= 180), "Longitude out of range."
    
    lat = radians(90-lat)
    lon = radians(lon)
    alt = 6371000.8+alt*0.3048 # in meters
    
    S = alt*sin(lat)
    x = S*cos(lon)
    y = S*sin(lon)
    z = alt*cos(lat)
    
    if home is not None:
        chome = spherical2cartesian(home)
        x -= chome[0]
        y -= chome[1]
        z -= chome[2]
    
    return (x,y,z)

"""
x, y, z = spherical2cartesian((51,1,900))
cartesian2spherical((x,y,z))
"""
def cartesian2spherical(coord, home=None):
    x, y, z = coord
    
    if home is not None:
        chome = spherical2cartesian(home)
        x += chome[0]
        y += chome[1]
        z += chome[2]
    
    alt = sqrt(x**2+y**2+z**2)
    assert(alt>0), "Altitude of 0 is not possible."
    lat = 90-degrees(acos(z/alt))
    # east/west, north/south?
    if   y > 0: lon = 90-degrees(atan(x/y))
    elif y < 0: lon = -90-degrees(atan(x/y))
    else:
        if x >= 0: lon = 0.0
        else:      lon = 180
    
    return (lat, lon, (alt-6371000.8)/0.3048)

def central_angle(coord1, coord2):
    lat1 = radians(coord1[0])
    lat2 = radians(coord2[0])
    
    lon1 = radians(coord1[1])
    lon2 = radians(coord2[1])
    
    tmp1 = sin((lat2-lat1)/2)**2
    tmp2 = cos(lat1)*cos(lat2)*sin((lon2-lon1)/2)**2
    
    angle = 2*asin(sqrt(tmp1+tmp2))
    
    return angle # in radians

def distance3D(coord1, coord2, K=1):
    """
    Calculate the 3D distance between these two coordinates.
    Altitude is given in feet. Distance is returned in meters.
    Assumption: Earth is a sphere.
    """
    
    angle = central_angle(coord1, coord2)
    radius = K * 6371000.8
    
    # law of cosines
    a = radius+coord1[2]*0.3048
    b = radius+coord2[2]*0.3048
    c_squared = a**2 + b**2 - 2*a*b*cos(angle)
    
    return sqrt(c_squared)

def elevation_angle(coord1, coord2, K=1):
    """
    Calculate the angle between the radio horizon line
    and the straight line from coord1 to coord2 at
    coord1
    """
    horizon = (4.12*sqrt(coord1[2]*0.3048) + 4.12*sqrt(coord2[2]*0.3048)) * 1000
    dist3d = distance3D(coord1, coord2, K)
    radius = K * 6371000.8
    lt = radius+coord1[2]*0.3048
    la = radius+coord2[2]*0.3048
    
    alpha1 = acos((la**2-horizon**2-lt**2)/(2*horizon*lt))
    alpha2 = acos((la**2-dist3d**2-lt**2)/(2*dist3d*lt))
    
    return alpha1-alpha2 # in radians
    
def haversine(coord1, coord2, K=1):
    """
    Calculate the great circle distance between two points
    on the earth (specified in decimal degrees) in meters
    """
    
    meters = K * 6371000.8 * central_angle(coord1, coord2)
    
    return meters

def move_towards(actual, target, stepsize):
    """
    Calculate the geographic coordinates when moving
    stepsize meters from the actual position towards
    the target position.
    """
    
    # vector representing the direction
    direction = (target[0]-actual[0], target[1]-actual[1])
    # length of this vector in meters
    dist = haversine(actual, target)
    
    if dist < stepsize:
        return target
    
    # calculate coordinates
    latitude = actual[0] + direction[0]/dist*stepsize
    longitude = actual[1] + direction[1]/dist*stepsize
    
    return (latitude, longitude)

def get_true_course(position, destination):
    """
    Calculates the course to fly to the given
    destination from the given position. It is
    defined as the angle in degrees clockwise
    from geographic north
    
    Src: http://www.movable-type.co.uk/scripts/latlong.html
    """
    
    # parameters in radians
    lat1 = radians(position[0])
    lat2 = radians(destination[0])
    dLon = radians(destination[1]-position[1])
    
    y = sin(dLon) * cos(lat2)
    x = cos(lat1)*sin(lat2) - \
        sin(lat1)*cos(lat2)*cos(dLon);
        
    angle = degrees(atan2(y, x))
    
    # finally transfer angle
    return angle%360


def get_magnetic_course(position, destination):
    """
    Calculates the heading to fly to the given
    destination from the given position. It is
    defined as the angle in degrees clockwise
    from magnetic north.
    """
    
    magnetic_north = (82.7, -114.4)
    true_course = get_true_course(position, destination)
    variation = get_true_course(position, magnetic_north)
    
    return (true_course-variation)%360

def get_ew_ns_velocity(actual, target, duration):
    """
    Return the EW- and NS-velocity which is necessary
    to move from the actual position to the target
    position (given in coordinates) in the given
    time in meters.
    """
    
    # get the vertical distance in NS-direction in meters
    vert_dist = haversine(actual, (target[0], actual[1]))
    # get the horizontal distance in EW-rirection in meters
    horiz_dist = haversine(actual, (actual[0], target[1]))
    
    # NS or SN?
    if target[0] > actual[0]:
        vert_dist *= -1.0 # change sign if SN
    
    # EW or WE?
    if target[1] > actual[1]:
        horiz_dist *= -1.0 # change sign if WE
    
    return (horiz_dist/duration, vert_dist/duration)