const devices = {
	'iPhone-15': {
		title: 'iPhone 15',
		iframe: { width: 393, height: 852, marginTop: 20, marginLeft: 24, borderRadius: 20 },
		bezel: { width: 445, height: 897 },
		type: 'phone'
	},
	'iPhone-15-Plus': {
		title: 'iPhone 15 Plus',
		iframe: { width: 430, height: 932, marginTop: 23, marginLeft: 24, borderRadius: 20 },
		bezel: { width: 487, height: 983 },
		type: 'phone'
	},
	'Google-Pixel-8': {
		title: 'Google Pixel 8',
		iframe: { width: 412, height: 915, marginTop: 17, marginLeft: 16, borderRadius: 20 },
		bezel: { width: 452, height: 950 },
		type: 'phone'
	},
	none: {
		title: 'None',
		iframe: { width: '100%', height: '100%', position: 'relative' },
		bezel: { width: '100%', height: '100%', position: 'relative' },
		type: 'desktop'
	},
	'Desktop-HIDPI': {
		title: 'Desktop HIPDI',
		iframe: { width: 1920, height: 1080, marginTop: 63, marginLeft: 63 },
		bezel: { width: 2046, height: 1361 },
		type: 'desktop'
	}
};

let index = 0;
for (const device in devices) {
	devices[device].id = device;
	devices[device].index = index++;
}

export default devices;
