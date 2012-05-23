usr = {
  populate: function() {
	  usr.model = Backbone.Model.extend({
		  urlRoot: "users/user",
	  });	  
	  
	  usr.model.fetch();
	  
  }		
		
}