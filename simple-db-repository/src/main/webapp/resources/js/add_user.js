usr = {
  init:	function(base_path) {
	  sdb_webapp_base = base_path;
  },

  populate: function() {
	$("#cancel").click(usr.cancel);
	$("#add").click(usr.add);
  },
  
  add: function() {
	  usr.results = {};
	  $("input").each(usr.process_input);  
	  $("select").each(usr.process_input);
	  var errors = false;
	  var em = usr.results["email"];
	  if (em == null || em.trim() == "") {
		  errors = true;
		  $("#email_errors").text("You must enter an email address");
	  } else {
		  $("#email_errors").text("");
	  }
	  var pw1 = usr.results["password"];
	  var pw2 = usr.results["password_repeat"];
	  if (pw1 == null || pw1.trim() == "") {
		  errors = true;
		  $("#password_errors").text("You must set a password for a new user");
	  } else if (pw1 != pw2) {
		  errors = true;
		  $("#password_errors").text("The passwords given must match");
	  } else {
		  $("#password_errors").text("");
	  }
	  if (errors)
		  return;
	  else {
		var u = new usr.User(usr.results);
		u.save();
		usr.nav_to_user_list();
	  }  
  },
  
  cancel: function() {
	  usr.nav_to_user_list();
  },
  
  nav_to_user_list: function() {
	  window.location.href = sdb_webapp_base + "/users/manageUsers";
  },
  
  process_input: function(index, element) {
	  var id = $(element).attr("id");
	  var i = id.indexOf("_field");
	  if (i > -1) {
		  var field = id.substring(0, i);
		  usr.results[field] = $(element).val();
	  }
  }
}

usr.User = Backbone.Model.extend({
  urlRoot: "users/",
});

