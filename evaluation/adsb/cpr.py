#!/usr/bin/env python
#
# Copyright 2010, 2012 Nick Foster
# 
# This file is part of gr-air-modes
# 
# gr-air-modes is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 3, or (at your option)
# any later version.
# 
# gr-air-modes is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with gr-air-modes; see the file COPYING.  If not, write to
# the Free Software Foundation, Inc., 51 Franklin Street,
# Boston, MA 02110-1301, USA.
# 

import math, time
from exceptions import *
import coord

#this implements CPR position decoding and encoding.
#the decoder is implemented as a class, cpr_decoder, which keeps state for local decoding.
#the encoder is cpr_encode([lat, lon], type (even=0, odd=1), and surface (0 for surface, 1 for airborne))

#TODO: remove range/bearing calc from CPR decoder class. you can do this outside of the decoder.

latz = 15

def nz(ctype):
	return 4 * latz - ctype

def dlat(ctype):
	nzcalc = nz(ctype)
	if nzcalc == 0:
		return 360.0
	else:
		return 360.0 / nzcalc

def nl(declat_in):
	if abs(declat_in) >= 87.0:
		return 1.0
	return math.floor( (2.0*math.pi) * math.acos(1.0- (1.0-math.cos(math.pi/(2.0*latz))) / math.cos( (math.pi/180.0)*abs(declat_in) )**2 )**-1)

def dlon(declat_in, ctype):
	nlcalc = max(nl(declat_in)-ctype, 1)
	return 360.0 / nlcalc

def decode_lat(enclat, ctype, my_lat):
	tmp1 = dlat(ctype)
	tmp2 = float(enclat) / (2**17)
	j = math.floor(my_lat/tmp1) + math.floor(0.5 + ((my_lat % tmp1) / tmp1) - tmp2)

	return tmp1 * (j + tmp2)

def decode_lon(declat, enclon, ctype, my_lon):
	tmp1 = dlon(declat, ctype)
	tmp2 = float(enclon) / (2**17)
	m = math.floor(my_lon / tmp1) + math.floor(0.5 + ((my_lon % tmp1) / tmp1) - tmp2)

	return tmp1 * (m + tmp2)

def cpr_resolve_local(my_location, encoded_location, ctype):
	[my_lat, my_lon] = my_location
	[enclat, enclon] = encoded_location
	
	if my_location is None:
		raise CPRNoPositionError
	
	decoded_lat = decode_lat(enclat, ctype, my_lat)
	decoded_lon = decode_lon(decoded_lat, enclon, ctype, my_lon)

	return [decoded_lat, decoded_lon]

def cpr_resolve_global(evenpos, oddpos, mostrecent):
	
	dlateven = dlat(0)
	dlatodd  = dlat(1)
	
	evenpos = [float(evenpos[0]), float(evenpos[1])]
	oddpos = [float(oddpos[0]), float(oddpos[1])]
	
	j = math.floor(((nz(1)*evenpos[0] - nz(0)*oddpos[0])/2**17) + 0.5) #latitude index
	
	rlateven = dlateven * ((j % nz(0))+evenpos[0]/2**17)
	rlatodd  = dlatodd  * ((j % nz(1))+ oddpos[0]/2**17)
	
	#limit to -90, 90
	if rlateven > 270.0:
		rlateven -= 360.0
	if rlatodd > 270.0:
		rlatodd -= 360.0
		
	#This checks to see if the latitudes of the reports straddle a transition boundary
	#If so, you can't get a globally-resolvable location.
	if nl(rlateven) != nl(rlatodd):
		raise CPRBoundaryStraddleError
	
	if mostrecent == 0:
		rlat = rlateven
	else:
		rlat = rlatodd
		
	dl = dlon(rlat, mostrecent)
	nl_rlat = nl(rlat)
	
	m = math.floor(((evenpos[1]*(nl_rlat-1)-oddpos[1]*nl_rlat)/2**17)+0.5) #longitude index
	
	#when surface positions straddle a disambiguation boundary (90 degrees),
	#surface decoding will fail. this might never be a problem in real life, but it'll fail in the
	#test case. the documentation doesn't mention it.
	
	if mostrecent == 0:
		enclon = evenpos[1]
	else:
		enclon = oddpos[1]
		
	rlon = dl * ((m % max(nl_rlat-mostrecent,1)) + enclon/2.**17)
	
	#limit to (-180, 180)
	if rlon > 180:
		rlon -= 360.0

	return [rlat, rlon]

class cpr_decoder:
	def __init__(self, my_location=None):
		self.my_location = my_location
		self.evenlist = {}
		self.oddlist = {}
		self.locations = {}
		
	def set_location(self, new_location):
		self.my_location = new_location

	def weed_poslists(self, timestamp=None):
		for poslist in [self.evenlist, self.oddlist]:
			for key, item in poslist.items():
				if (timestamp or time.time()) - item[2] > 10:
					del poslist[key]
		
		for key, item in self.locations.items():
			if (timestamp or time.time()) - item[2] > 300:
				del self.locations[key]

	def decode(self, icao24, encoded_lat, encoded_lon, cpr_format, timestamp=None, use_local=True):

		#add the info to the position reports list for global decoding
		if cpr_format==1:
			self.oddlist[icao24] = [encoded_lat, encoded_lon, timestamp or time.time()]
		else:
			self.evenlist[icao24] = [encoded_lat, encoded_lon, timestamp or time.time()]

		#okay, let's traverse the lists and weed out those entries that are older than 10 seconds
		self.weed_poslists(timestamp)
		
		if (icao24 in self.evenlist) \
		 and (icao24 in self.oddlist) \
		   and (icao24 not in self.locations):
			#figure out which report is newer
			newer = (self.oddlist[icao24][2] - self.evenlist[icao24][2]) > 0
			[glat, glon] = cpr_resolve_global(self.evenlist[icao24][0:2],
											  self.oddlist[icao24][0:2],
											  newer) #do a global decode
			
			# plausability check: range is unlikely to be more than 500km
			if coord.haversine(self.my_location, [glat, glon]) > 500000:
#				print "Warning: avoided wrong global decoding."
				raise CPRNoPositionError
			
			if not use_local:
				return [glat, glon] # activate this to exclusively use global decoding
			self.locations[icao24] = [glat, glon, timestamp or time.time()]
		
		
		if icao24 in self.locations:
			location = self.locations[icao24][0:2]
		else:
			location = self.my_location
		
		[llat, llon] = cpr_resolve_local(location,
										 [encoded_lat, encoded_lon],
										 cpr_format)
		if icao24 in self.locations \
		 or use_local and coord.haversine(self.my_location, [llat, llon]) < 300000: # simple local decoding for close flight is ok
			self.locations[icao24] = [llat, llon, timestamp or time.time()]
		else:
			raise CPRNoPositionError

		return [llat, llon]

#encode CPR position
def cpr_encode(lat, lon, ctype, surface):
	if surface is True:
		scalar = 2.**19
	else:
		scalar = 2.**17

	#encode using 360 constant for segment size.
	dlati = dlat(ctype, False)
	yz = math.floor(scalar * ((lat % dlati)/dlati) + 0.5)
	rlat = dlati * ((yz / scalar) + math.floor(lat / dlati))

	#encode using 360 constant for segment size.
	dloni = dlon(lat, ctype, False)
	xz = math.floor(scalar * ((lon % dloni)/dloni) + 0.5)

	yz = int(yz) & (2**17-1)
	xz = int(xz) & (2**17-1)

	return (yz, xz) #lat, lon
