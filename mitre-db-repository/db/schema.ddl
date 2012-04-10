use userdb; 	
	
drop table user_roles;
	drop table user_attributes;
	drop table users;
	drop table roles;

    create table USERS (
        USER_ID int not null auto_increment,
        CONFIRMATION_HASH varchar(128),
        EMAIL varchar(128),
        CONFIRMED tinyint,
        FAILED_ATTEMPTS smallint,
        PASSWORD_HASH varchar(128) not null,
        PASSWORD_SALT smallint,
        USERNAME varchar(48) not null unique,
        primary key (USER_ID)
    );

    create table USER_ROLES (
        USER_ID int not null,
        ROLE_ID int not null,
        primary key (USER_ID, ROLE_ID)
    );

    create table ROLES (
        ROLE_ID int not null auto_increment,
        ROLE_NAME varchar(32),
        ROLE_DESCRIPTION varchar(1024),
        primary key (ROLE_ID)
    );

    alter table USER_ROLES 
        add constraint FK_ROLES_ROLE_ID 
        foreign key (ROLE_ID) 
        references ROLES(ROLE_ID);

    alter table USER_ROLES 
        add constraint FK_ROLES_USER_ID 
        foreign key (USER_ID) 
        references USERS(USER_ID);

	create table USER_ATTRIBUTES (
		ATTRIBUTE_ID int not null auto_increment,
		USER_ID int not null,
		ATTR_NAME varchar(64) not null,
		IS_TOKEN tinyint,
		ATTR_VALUE varchar(1024),
		ACCESS_TOKEN varchar(1024),
		TOKEN_EXPIRATION datetime,
		primary key (ATTRIBUTE_ID)
	);
	
	alter table USER_ATTRIBUTES
		add constraint FK_USER_ATTRIBUTES
		foreign key (USER_ID)
		references USERS(USER_ID);
