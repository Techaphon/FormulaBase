package com.formulabase;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
class PrimaryAdminMigrationTest {
 @Test void migrationProtectsOneActiveCeoAndKeepsExistingUsers() throws Exception {
  try(var c=DriverManager.getConnection("jdbc:h2:mem:adminmigration;MODE=PostgreSQL");var s=c.createStatement()) {
   s.execute("create table users(id uuid primary key, username varchar(80), role varchar(20), active boolean, archived boolean)");
   s.execute("insert into users values(random_uuid(),'other','CEO',true,false),(random_uuid(),'ceo','CEO',true,false),(random_uuid(),'worker','WORKER',true,false)");
   try(var stream=getClass().getResourceAsStream("/db/migration/V9__protect_primary_admin.sql")) {
    for(String sql:new String(stream.readAllBytes(),StandardCharsets.UTF_8).split(";"))if(!sql.isBlank())s.execute(sql);
   }
   var count=s.executeQuery("select count(*) from users");count.next();assertEquals(3,count.getInt(1));
   var primary=s.executeQuery("select username from users where primary_admin=true");assertTrue(primary.next());assertEquals("ceo",primary.getString(1));assertFalse(primary.next());
  }
 }
}
