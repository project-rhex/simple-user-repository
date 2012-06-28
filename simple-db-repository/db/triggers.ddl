drop trigger if exists USERS_INS_Trigger;

delimiter $$

CREATE TRIGGER USERS_INS_Trigger AFTER insert ON USERS
  FOR EACH ROW
    BEGIN
        DECLARE nhind_test_count INT;
        DECLARE username_trim VARCHAR(20);

        loop_it: LOOP

        SELECT COUNT(*) into @nhind_test_count FROM information_schema.tables  WHERE table_schema = 'nhind';
        if @nhind_test_count = 0 then
           leave loop_it;
        end if;  

        SELECT COUNT(*) into @nhind_test_count FROM information_schema.tables  WHERE table_schema = 'nhind' AND table_name = 'address';
        if @nhind_test_count = 0 then
           leave loop_it;
        end if;  

        SELECT COUNT(*) into @nhind_test_count FROM information_schema.tables  WHERE table_schema = 'james_mail_userdb';
        if @nhind_test_count = 0 then
           leave loop_it;
        end if;  

        SELECT COUNT(*) into @nhind_test_count FROM information_schema.tables  WHERE table_schema = 'james_mail_userdb' AND table_name = 'users';
        if @nhind_test_count = 0 then
           leave loop_it;
        end if;  

        set username_trim := SUBSTRING_INDEX(NEW.username, "@", 1);
        insert into james_mail_userdb.users (username, pwdHash, pwdAlgorithm, useForwarding, forwardDestination, useAlias, alias) values (username_trim, NEW.JAMES_PASSWORD_HASH, "SHA", 0, NULL, 0, NULL);

        SELECT AUTO_INCREMENT into @id FROM information_schema.TABLES WHERE table_schema = 'nhind' AND table_name = 'address';
        insert into nhind.address (id, eMailAddress, domainId, displayName, status, updateTime) values 
                                  (@id, NEW.username, 1, NEW.username, 1, now());

        leave loop_it;
        END LOOP loop_it;

    END$$

delimiter ;

drop trigger if exists USERS_UPD_Trigger;

delimiter $$

CREATE TRIGGER USERS_UPD_Trigger AFTER update ON USERS
  FOR EACH ROW
    BEGIN
        DECLARE nhind_test_count INT;

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

drop trigger if exists USERS_DEL_Trigger;
delimiter $$
CREATE TRIGGER USERS_DEL_Trigger BEFORE delete ON USERS
  FOR EACH ROW
    BEGIN
        DECLARE nhind_test_count INT;
        DECLARE username_trim VARCHAR(20);

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
