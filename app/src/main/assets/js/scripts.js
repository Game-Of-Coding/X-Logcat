var div_container = document.getElementById("container");
var span_noLogWarnAvailElem =  document.getElementById("no_log_warning");
var previousClassType = "white";

// Scrolls down to the bottom of the page
function scrollToBottom() {
	window.scrollTo(0, div_container.scrollHeight);
}

// Updates webpage logs, called from java code
function updateLogs(type, logLine, scroll) {
	// Remove log warning if has
	if (document.body.contains(span_noLogWarnAvailElem))
		div_container.removeChild(span_noLogWarnAvailElem)
	var typeClass;
	switch (type)
	{
		case 0: // V
			typeClass = "verbose";
			break;
		case 1: // D
			typeClass = "debug";
			break;
		case 2: // I
			typeClass = "info";
			break;
		case 3: // W
			typeClass = "warn";
			break;
		case 4: // E
			typeClass = "error";
			break;
	}
	// Element for breaking to span tags
	if (previousClassType != typeClass)
	{
		// Draw line
		var hrElement = document.createElement("hr");
		hrElement.classList.add(previousClassType + "_line");
		previousClassType = typeClass;
		div_container.appendChild(hrElement);
	}
	else
	{
		// Append br tag
		var brElement = document.createElement("br");
		div_container.appendChild(brElement);
	}

	// Add element that shows log
	var spanElement = document.createElement("span");
	spanElement.innerHTML = logLine;
	spanElement.classList.add(typeClass);
	div_container.appendChild(spanElement);

	// Scroll if needed
	if (scroll)
		window.scrollBy(0, 40);
}
