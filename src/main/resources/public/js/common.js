
	const toggle = function(id) {
		let x = document.getElementById(id);
		  if (x.style.display === "none") {
		    x.style.display = "block";
		  } else {
		    x.style.display = "none";
		  }
	}

	const hide = function(id) {
		let x = document.getElementById(id);
	    x.style.display = "none";
	}

    const loopCheckStatus = async function(oper) {
	    const statusResult = await checkStatus(oper);
		console.log("CheckStatus:" + statusResult)
	    if (statusResult === "COMPLETED") {
	    	console.log("Stop checking result")
	        doComplete(oper)
	    }
	    else {
	    	setTimeout(function() {loopCheckStatus(oper);}, 3000);
	    }
	}

	const checkStatus = async function(oper) {
	    const statusResponse = await fetch(window.location.origin+'/web/' + oper + '/checkStatus');
	    if (statusResponse.ok) {
	       const statusResult = await statusResponse.text();
	       return statusResult;
	    }
	    else {
	       throw Error(statusResponse.statusText);
	    }
	}

	const doComplete = async function(oper) {
	    const completeResponse = await fetch(window.location.origin+'/web/' + oper + '/doComplete');
	    if (completeResponse.ok) {
	       const jsonObject = await completeResponse.json();
	       document.getElementById(oper+"CompleteResponse").value = JSON.stringify(jsonObject, null, 2);
	    }
	    else {
	       throw Error(completeResponse.statusText);
	    }
	}

	const reportError = async function(oper, message) {
       document.getElementById(oper+"CompleteResponse").value = message;
	}
