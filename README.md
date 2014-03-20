Using the same code from the "Android GPS" project, this application implements a 2D barcode scanner, provided by Zebra Crossing Project (ZXing), in order to read barcodes and extract information about an exhibit or monument or any place of interest, as well it's coordinates. The barcode information, must me a text with this format:

XXX.XXXXX,YYY.YYYYY|abc123

where

X = the latitude of the spot (decimal format by WGS84).
Y = the longitude of the spot (decimal format by WGS84).
abc123 = Any name, description of whatever suits for this spot.