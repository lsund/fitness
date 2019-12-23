create table exerciseid_name (id serial primary key, exerciseid int not null, name text not null);
alter table exercise drop column name;
