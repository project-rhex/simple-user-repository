drop table if exists users_roles;
drop table if exists user_attributes;
drop table if exists users;
drop table if exists roles;

create table USERS (
    USER_ID int not null auto_increment,
    CONFIRMATION_HASH varchar(128),
    EMAIL varchar(128),
    CONFIRMED tinyint,
    FAILED_ATTEMPTS smallint,
    PASSWORD_HASH varchar(128) not null,
    JAMES_PASSWORD_HASH varchar(128) not null,
    PASSWORD_SALT int,
    USERNAME varchar(48) not null unique,
    UPDATED datetime,
    primary key (USER_ID)
);

create table USERS_ROLES (
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

alter table USERS_ROLES 
    add constraint FK_UR_ROLE_ID 
    foreign key (ROLE_ID) 
    references ROLES(ROLE_ID);

alter table USERS_ROLES 
    add constraint FK_UR_USER_ID 
    foreign key (USER_ID) 
    references USERS(USER_ID);

create table USER_ATTRIBUTES (
	ID int not null auto_increment,
	USER_ID int,
	ATTR_NAME varchar(64) not null,
	ATTR_TYPE smallint,
	ATTR_VALUE varchar(1024),
	ACCESS_TOKEN varchar(1024),
	TOKEN_EXPIRATION datetime,
	primary key (ID)
);

alter table USER_ATTRIBUTES
	add constraint FK_USER_ATTRIBUTES
	foreign key (USER_ID)
	references USERS(USER_ID);


/* trigger debug 
create table tb (
    log varchar(64)
);
*/

drop trigger USERS_INS_Trigger;
delimiter $$
CREATE TRIGGER USERS_INS_Trigger AFTER insert ON USERS
  FOR EACH ROW
    BEGIN
        DECLARE nhind_test_count INT;
        DECLARE username_trim VARCHAR(20);

        -- adding a loop, so can break out of it
        loop_it: LOOP

        --        insert into tb set log="=========";
        -- insert into tb set log="GG1";
        SELECT COUNT(*) into @nhind_test_count FROM information_schema.tables  WHERE table_schema = 'nhind';
        if @nhind_test_count = 0 then
           leave loop_it;
        end if;  

        -- insert into tb set log="GG2";
        SELECT COUNT(*) into @nhind_test_count FROM information_schema.tables  WHERE table_schema = 'nhind' AND table_name = 'address';
        if @nhind_test_count = 0 then
           leave loop_it;
        end if;  

        -- insert into tb set log="GG3";
        SELECT COUNT(*) into @nhind_test_count FROM information_schema.tables  WHERE table_schema = 'james_mail_userdb';
        if @nhind_test_count = 0 then
           leave loop_it;
        end if;  

        -- insert into tb set log="GG4";
        SELECT COUNT(*) into @nhind_test_count FROM information_schema.tables  WHERE table_schema = 'james_mail_userdb' AND table_name = 'users';
        if @nhind_test_count = 0 then
           leave loop_it;
        end if;  

        -- insert into tb set log="GG5";
        set username_trim := SUBSTRING_INDEX(NEW.username, "@", 1);
        insert into james_mail_userdb.users (username, pwdHash, pwdAlgorithm, useForwarding, forwardDestination, useAlias, alias) values (username_trim, NEW.JAMES_PASSWORD_HASH, "SHA", 0, NULL, 0, NULL);

        -- insert into tb set log="GG6";
        SELECT AUTO_INCREMENT into @id FROM information_schema.TABLES WHERE table_schema = 'nhind' AND table_name = 'address';

        -- SET creationDate := now();   
        -- Set updateDate := now();
        -- insert into tb set log="GG7";
        insert into nhind.address (id, eMailAddress, domainId, displayName, status, updateTime) values \
                                  (@id, NEW.username, 1, NEW.username, 1, now());

        -- insert into tb set log="GG8";

        leave loop_it;
        END LOOP loop_it;

    END$$
delimiter ;

drop trigger USERS_UPD_Trigger;
delimiter $$
CREATE TRIGGER USERS_UPD_Trigger AFTER update ON USERS
  FOR EACH ROW
    BEGIN
        DECLARE nhind_test_count INT;

        -- adding a loop, so can break out of it
        loop_it: LOOP

        SELECT COUNT(*) into @nhind_test_count FROM information_schema.tables  WHERE table_schema = 'james_mail_userdb';
        if @nhind_test_count = 0 then
           leave loop_it;
        end if;  

        SELECT COUNT(*) into @nhind_test_count FROM information_schema.tables  WHERE table_schema = 'james_mail_userdb' AND table_name = 'users';
        if @nhind_test_count = 0 then
           leave loop_it;
        end if;  


        update james_mail_userdb.users set pwdHash = NEW.JAMES_PASSWORD_HASH where username = NEW.username;

        leave loop_it;
        END LOOP loop_it;

    END$$
delimiter ;

drop trigger USERS_DEL_Trigger;
delimiter $$
CREATE TRIGGER USERS_DEL_Trigger BEFORE delete ON USERS
  FOR EACH ROW
    BEGIN
        DECLARE nhind_test_count INT;
        DECLARE username_trim VARCHAR(20);

        -- adding a loop, so can break out of it
        loop_it: LOOP

        SELECT COUNT(*) into @nhind_test_count FROM information_schema.tables  WHERE table_schema = 'james_mail_userdb';
        if @nhind_test_count = 0 then
           leave loop_it;
        end if;  

        SELECT COUNT(*) into @nhind_test_count FROM information_schema.tables  WHERE table_schema = 'james_mail_userdb' AND table_name = 'users';
        if @nhind_test_count = 0 then
           leave loop_it;
        end if;  

        SELECT COUNT(*) into @nhind_test_count FROM information_schema.tables  WHERE table_schema = 'nhind';
        if @nhind_test_count = 0 then
           leave loop_it;
        end if;  

        -- insert into tb set log="GG2";
        SELECT COUNT(*) into @nhind_test_count FROM information_schema.tables  WHERE table_schema = 'nhind' AND table_name = 'address';
        if @nhind_test_count = 0 then
           leave loop_it;
        end if;  

        set username_trim := SUBSTRING_INDEX(OLD.username, "@", 1);    
        delete from james_mail_userdb.users where username = username_trim;
        delete from nhind.address where eMailAddress = OLD.username;

        leave loop_it;
        END LOOP loop_it;

    END$$
delimiter ;
