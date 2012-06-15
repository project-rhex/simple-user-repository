users = {
	set_base: function(base_path) {
		users.base_path = base_path;
	},
		
	loader: function(page, sort_column) {
		users.userList = new users.UserList();
		users.userList.fetch({success: function(retrievedList) {
			var ulv = new users.UserListView({collection: retrievedList});
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
			var user = new users.User({"user-id": uid});
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

users.User = Backbone.Model.extend({
  url: function() {
	if (this.id != null) {
		return "users/" + this.id;
	} else {
		return "users/";
	}
  },
  idAttribute: "user-id"
});

users.UserList = Backbone.Collection.extend({
  url: "users/",
  model: users.User
}),
	
users.UserView = Backbone.View.extend({
	tagName: 'tr',
	template: _.template('<td><%= userId %></td><td><a href="./users/editUser/<%= userId %>' +
	'"><%= givenName %></a></td><td><%= familyName %></td><td><%= extendedProperties.TITLE %></td><td><%= email %></td>' +
	'<td>unknown</td><td><a href="#" data-user-id="<%= extendedProperties._USER_ID %>">' +
	'<img height="20" width="20" src="./resources/images/delete.png"></a></td>' ),
	render: function(){
		var attributes = this.model.toJSON();
		if (typeof(attributes.givenName) == 'undefined') {
			attributes.givenName = '';
		}
		if (typeof(attributes.familyName) == 'undefined') {
			attributes.familyName = '';
		}
		if (typeof(attributes.extendedProperties.TITLE) == 'undefined') {
			attributes.extendedProperties.TITLE = '';
		}
		this.$el.html(this.template(attributes));
	}
});

users.UserListView = Backbone.View.extend({
  tagName: 'tbody',
  initialize: function(){
    this.collection.on('add', this.addOne, this);
  },
  addOne: function(user){
    var userView = new users.UserView({model: user});
    userView.render();
    this.$el.append(userView.el);
  },
  render: function(){
	$(this.el).append("<tr><th>Username</th><th>First Name</th><th>Last Name</th><th>Title</th><th>Email</th><th>Last Login</th><th></th></tr>");
    this.collection.forEach(this.addOne, this);
  }
});

$(document).ready(function() {
	var page = $("#page").val();
	var sort = $("#sort_on").val();
	users.set_base("${base}");
	users.loader(page, sort);
	users.paginator(page, sort);
});