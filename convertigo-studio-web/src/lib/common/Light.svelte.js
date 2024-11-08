import { browser } from '$app/environment';

let light = $state(false);
let init = false;

function checkInit() {
	if (!init) {
		if (browser) {
			init = true;
			setLight(
				!(
					localStorage.theme === 'dark' ||
					(!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)
				)
			);
		}
	}
}

function setLight(value) {
	console.log('setLight', value);
	light = value;
	if (browser) {
		console.log('setLightBrowser', value);
		localStorage.theme = light ? 'light' : 'dark';
		document.documentElement.classList.toggle('dark', !light);
	}
}

export default {
	get light() {
		checkInit();
		return light;
	},
	set light(value) {
		setLight(value);
	},
	get dark() {
		checkInit();
		return !light;
	},
	get mode() {
		checkInit();
		return light ? 'light' : 'dark';
	}
};
