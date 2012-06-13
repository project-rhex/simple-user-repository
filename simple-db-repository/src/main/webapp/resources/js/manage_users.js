users = {
		
	set_base: function(base_path) {
		users.base_path = base_path;
	},
		
	loader: function(page, sort_column) {
		users.userList = new UserList();
		users.userList.fetch({success: function(retrievedList) {
			var ulv = new UserListView({collection: retrievedList});
			ulv.render();
			$('#people').html(ulv.el);
			$("a[data-user-id]").each(function(index, el) {
				$(el).parent().parent().attr("data-user-id", $(el).attr("data-user-id"));
				$(el).click(users.do_delete);
			});	
		}
		});
	},
	
	paginator: function(page, sort_column) {
	},
	
	paginator_insert: function(text) {
		$("#paginator").append(text);
	},
	
	do_delete: function() {
		if (confirm("Really delete user?")) {
			var tr = $(this).parent().parent();
			var uid = tr.attr("data-user-id");
			var user = new User({"user-id": uid});
			user.destroy({
			success: function(data, stat, xhr) {
				tr.remove();
			},
			error: function(xhr, stat, error) {
				alert("error");
			}
			});
		}
		return false;
	}
}

User = Backbone.Model.extend({
	  url: function() {
		if (this.id != null) {
			return "users/" + this.id;
		} else {
			return "users/";
		}
	  },
	  idAttribute: "user-id"
});

UserList = Backbone.Collection.extend({
	  url: "users/",
	  model: User
});

UserView = Backbone.View.extend({
	tagName: 'tr',
	template: _.template('<td><a href="./users/editUser/<%= userId %>' +
	'"><%= givenName %></a></td><td><%= familyName %></td><td><%= extendedProperties.TITLE %></td><td><%= email %></td>' +
	'<td>unknown</td><td><a href="#" data-user-id="<%= userId %>"><img height="20" width="20" src="./resources/images/delete.png"></a></td>' ),
	render: function(){
		var attributes = this.model.toJSON();
		this.$el.html(this.template(attributes));
	}
});

var UserListView = Backbone.View.extend({
	  tagName: 'tbody',
	  initialize: function(){
	    this.collection.on('add', this.addOne, this);
	  },
	  addOne: function(user){
	    var userView = new UserView({model: user});
	    userView.render();
	    this.$el.append(userView.el);
	  },
	  render: function(){
		$(this.el).append("<tr><th>First Name</th><th>Last Name</th><th>Title</th><th>Email</th><th>Last Login</th><th></th></tr>");
	    this.collection.forEach(this.addOne, this);
	  }
});