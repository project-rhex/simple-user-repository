usr = {
  User: Backbone.Model.extend({
	urlRoot: "users/",
	idAttribute: "user-id"
  }),
		
  init:	function(base_path) {
	  sdb_webapp_base = base_path;
  },

  populate: function() {
	$("#cancel").click(usr.cancel);
	$("#add").click(usr.add);
	$("#addProperty").click(usr.addProperty);
  },
  
  add: function() {
	  var user_id = parseInt($("#user_id").val());
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
	  /* Only check passwords for new users */
	  if (user_id < 0) {
		  if (pw1 == null || pw1.trim() == "") {
			errors = true;
			$("#password_errors").text("You must set a password for a new user");
		  } else {
		    $("#password_errors").text("");
		  }
	  }
	  if (pw1 != pw2) {
		errors = true;
		$("#password_errors").text("The passwords given must match");
	  } else {
		$("#password_errors").text("");
	  }
	  if (errors)
		  return;
	  else {
		var u = new usr.User(usr.results);
		if (user_id > -1) {
			u.set("user-id", user_id);
		}
		u.save();
		usr.nav_to_user_list();
	  }  
  },
  
  addProperty: function() {
	  var property = prompt("Enter the name of the property to add", "");
	  if (property == null || property == "") {
		  alert("Property can't be an empty string");
		  return;
	  }
	  property = property.toLowerCase();
	  property = property.replace(" ","_");
	  
	  for(var i = 0; i < usr.properties.length; i++) {
		  if (property == usr.properties[i]) {
			  alert("That property already exists");
			  return;
		  }
	  }
	  usr.properties[usr.properties.length] = property;
	  plabel = property.replace("_", " ");
	  plabel = plabel.substr(0,1).toUpperCase() + plabel.substr(1);
	  pfield = property + "_field";
	  
	  $("#property_marker").after('<div class="row"><label class="span2" for=' + pfield + '">' + plabel 
			  + '</label><input id="' + pfield + '" class="span4" size="40" value=""></div>');
  },
  
  cancel: function() {
	  usr.nav_to_user_list();
  },
  
  nav_to_user_list: function() {
	  window.location.href = "./users/manageUsers";
  },
  
  process_input: function(index, element) {
	  var id = $(element).attr("id");
	  var i = id.indexOf("_field");
	  if (i > -1) {
		  var field = id.substring(0, i);
		  usr.results[field] = $(element).val();
	  }
  },
  
  properties: [ "title", "first_name", "last_name", "email", "password" ]
}

$(document).ready(function() {
	usr.init("${base}");
	usr.populate();
});
