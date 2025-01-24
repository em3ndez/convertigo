import { call, checkArray } from '$lib/utils/service';

let logs = $state([]);
let startDate = $state('');
let endDate = $state('');
let realtime = $state(false);
let nbLines = $state(100);
let moreResults = $state(false);
let calling = $state(false);

let lastCall = 0;

function formatDate(timestamp) {
	if (timestamp === null) return '';
	return new Date(timestamp).toISOString().split('T')[0];
}

function formatTime(timestamp) {
	if (timestamp === null) return '';
	return new Date(timestamp).toISOString().split('T')[1].split('Z')[0].replace('.', ',');
}

async function list(clear = false) {
	if (realtime && calling) {
		return;
	}
	if (clear) {
		logs = [];
		moreResults = false;
	}
	if (!realtime && !moreResults && !clear) {
		return;
	}
	calling = true;
	const currentCall = ++lastCall;
	try {
		const res = await call('logs.Get', {
			moreResults,
			startDate,
			endDate,
			realtime,
			nbLines
		});
		if (currentCall == lastCall) {
			moreResults = res?.hasMoreResults ?? false;
			logs.push(...checkArray(res?.lines));
		}
	} finally {
		if (currentCall == lastCall) {
			calling = false;
		}
	}
}

export default {
	get logs() {
		return logs;
	},
	get startDate() {
		return startDate;
	},
	set startDate(value) {
		startDate = value;
	},
	get endDate() {
		return endDate;
	},
	set endDate(value) {
		endDate = value;
	},
	get realtime() {
		return realtime;
	},
	set realtime(value) {
		realtime = value;
	},
	get nbLines() {
		return nbLines;
	},
	get moreResults() {
		return moreResults;
	},
	get calling() {
		return calling;
	},
	formatDate,
	formatTime,
	list
};