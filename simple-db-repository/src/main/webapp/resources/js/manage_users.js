User = Backbone.Model.extend({
	  url: "users/",
});

UserList = Backbone.Collection.extend({
	  url: "users/",
	  model: User
});

UserView = Backbone.View.extend({
	tagName: 'tr',
	template: _.template('<td><%= email %></td><td>unknown</td>'),
	render: function(){
		var attributes = this.model.toJSON();
		this.$el.html(this.template(attributes));
	}
});

var UserListView = Backbone.View.extend({
	  tagName: 'table',
	  initialize: function(){
	    this.collection.on('add', this.addOne, this);
	  },
	  addOne: function(user){
	    var userView = new UserView({model: user});
	    userView.render();
	    this.$el.append(userView.el);
	  },
	  render: function(){
	    this.collection.forEach(this.addOne, this);
} });


users = {
		
	set_base: function(base_path) {
		users.base_path = base_path;
	},
		
	loader: function(page, sort_column) {
		var userList = new UserList();
		userList.fetch({success: function(retrievedList) {
			var ulv = new UserListView({collection: retrievedList});
			ulv.render();
			$('#people').html(ulv.el);
		}
		});
	},
	
	paginator_insert: function(text) {
		$("#paginator").append(text);
	}	
}