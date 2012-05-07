users = {
	set_base: function(base_path) {
		users.base_path = base_path;
	},
		
	loader: function(page, sort_column) {
		var req = users.base_path + "/users/index?page=" + page + "&sort_on=" + sort_column;
		$.ajax({
			url: req,
			type: 'get',
	        dataType: 'json',
	        success: users.populate
		});
	},
	
	populate: function(resp) {
		var count = resp.count;
		var results = resp.results;
		$("#people").children().remove();
		$("#people").append("<tr><th id='col_ln'>Last Name</th><th id='col_fn'>First Name</th><th id='col_title'>Title</th><th id='col_em'>Email</th><th id='col_ll'>Last Login</th></tr>");
		for(var i = 0; i < count; i++) {
			var row = results[i];
			var first = row["FIRST_NAME"];
			var last = row["LAST_NAME"];
			var email = row["EMAIL"];
			var title = row["TITLE"];
			if (title == undefined) {
				title = '';
			}
			$("#people").append("<tr><td>" + last + "</td><td>" + first + "</td><td>" + title + "</td><td>" + email + "</td><td>unknown</td></tr>");
		}
		
	},
	
	paginator: function(page, sort_column) {
		var req = users.base_path + "/users/paginator?page=" + page + "&sort_on=" + sort_column;
		$.ajax({
			url: req,
			type: 'get',
	        dataType: 'text',
	        success: users.paginator_insert
		});
	},
	
	paginator_insert: function(text) {
		$("#paginator").append(text);
	}	
}