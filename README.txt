Tested with Samsung Galaxy S2 - custom ROM - 4.4.2 OS version. Tested with Huawei Mediapad 7 - 4.0.3 OS version.

This app is ment to show how to get frames from camera (recording not needed), get it's pixels and do what ever you want with it.
This app for example uses that pixels to send them to algorithm who compares them to the user given color and sends SMS
to the given number when the found(percentage of user given color on camera frame found) event occurs.

Issues with this app:
	-Android hardware not supposed to do that -> frames to big!? (640x480px picture - 134KB in size -> 307200 pixels)
	-When frame rate in values 1-5, Activity methods are to slow for them. Methods can be called several times. 
	-Also, frame rate not the same (slower giving of frames on weaker hardware).
	-custom Yum->RGB conversion very slow! Yum->JPEG->Bitmap working fine.
	-Color algorithm only uses RGB values in tolerance (20-40max). Maybe use HSV for better recognition?
	-colorTolerance, COLOR_MATCH_LIMIT, REQUIRED_SIZE (ScanActivity variables) are hardcoded. Give user a chance to set them.
	-BIGGEST ISSUE: -Has to be modified for specified user cases to work. (See Possible uses below)

Improvement needed in:
	-Color recognition in ScanActivity -> isSimilarToColors() method for checking given camera color pixels to the one set by the user.
	-Find a way to manipulate byte frame from camera (not the resized one). 
	-Implement mostCommonColor method in ColorPickCameraActivitiy for better color picker. (publicly available code)
	-Implement sound event on color found event!
	-What ever you want...
	
Possible uses:
	-Alarm system (in a dark room, sms-ing when something changes).
	-Colorblind help to recognize colors (shopping hair color, etc)
	-Counting things.

Hope it will help someone.

	
