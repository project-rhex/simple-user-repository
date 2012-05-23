usr = {
  populate: function() {
	var raw_json = $("#user").val();
	if (raw_json == null || raw_json == "") return
	var user = JSON.parse(raw_json);	  
  }		
		
}