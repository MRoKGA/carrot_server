
    create table region (
        id integer not null auto_increment,
        name varchar(10) not null,
        code varchar(20) not null,
        full_name varchar(100) not null,
        centroid POINT SRID 4326,
        primary key (id)
    ) engine=InnoDB;

    create table user_region (
        id integer not null auto_increment,
        is_active bit,
        is_primary bit,
        region_id integer not null,
        user_id integer not null,
        created_at datetime(6) not null,
        updated_at datetime(6),
        verified_at datetime(6),
        verify_expired_at datetime(6),
        primary key (id)
    ) engine=InnoDB;

    create table users (
        id integer not null auto_increment,
        manner_temperature float(53),
        created_at datetime(6) not null,
        updated_at datetime(6),
        nickname varchar(20) not null,
        phone_number varchar(20) not null,
        email varchar(50),
        profile_image_url varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table users 
       add constraint UK2ty1xmrrgtn89xt7kyxx6ta7h unique (nickname);

    alter table users 
       add constraint UK9q63snka3mdh91as4io72espi unique (phone_number);

    alter table users 
       add constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email);

    alter table user_region 
       add constraint FKkfdr7nd3nty4xxdgjfgdtl4b8 
       foreign key (region_id) 
       references region (id);

    alter table user_region 
       add constraint FKje1cyxoxymnfs8wtxn9qmkns4 
       foreign key (user_id) 
       references users (id);
