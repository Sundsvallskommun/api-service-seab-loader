
    create table invoice (
       id bigint not null auto_increment,
        content longtext,
        created datetime(6),
        invoice_id varchar(255),
        modified datetime(6),
        processed datetime(6),
        status varchar(255) not null,
        status_message longtext,
        primary key (id)
    ) engine=InnoDB;
create index invoice_invoice_id_index on invoice (invoice_id);

    alter table invoice 
       add constraint invoice_unique_invoice_id_constraint unique (invoice_id);
