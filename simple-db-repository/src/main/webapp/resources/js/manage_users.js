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
		$("#people").append("<tr><th data-sort='LAST_NAME'>Last Name</th><th data-sort='FIRST_NAME'>First Name</th><th data-sort='TITLE'>Title</th><th data-sort='EMAIL'>Email</th><th id='col_ll'>Last Login</th></tr>");
		for(var i = 0; i < count; i++) {
			var row = results[i];
			var first = row["FIRST_NAME"];
			var last = row["LAST_NAME"];
			var email = row["EMAIL"];
			var title = row["TITLE"];
			if (title == undefined) {
				title = '';
			}
			var user_id = row["ID"];
			last = "<a href='" + users.base_path + "/users/editUser?user=" + user_id + "'>" + last + "</a>";
			$("#people").append("<tr><td>" + last + "</td><td>" + first + "</td><td>" + title + "</td><td>" + email + "</td><td>unknown</td></tr>");
		}
		var sort = $("#sort_on").attr('value');
		var page = $("#page").attr("value");
		$('th[data-sort]').each(function(i, th){
			var sort_val = $(th).attr('data-sort');
			if (sort == sort_val) {
				var ipath = users.base_path + '/resources/images/downarrow.png';
				$(th).append("<img height='15' width='15' src='" + ipath + "'>");
			} else {
				var sort_path = users.base_path + "/users/manageUsers?page=" + page + "&sort_on=" + sort_val;
				$(th).click(function() {
					window.location = sort_path;
				});
				$(th).css('cursor', 'pointer');
			}
		});
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