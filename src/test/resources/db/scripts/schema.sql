
    create table invoice (
        municipality_id varchar(4),
        created datetime(6),
        id bigint not null auto_increment,
        modified datetime(6),
        processed datetime(6),
        invoice_id varchar(255),
        status varchar(255) not null,
        content longtext,
        status_message longtext,
        primary key (id)
    ) engine=InnoDB;

    create index invoice_invoice_id_index
       on invoice (invoice_id);

    create index invoice_status_index
       on invoice (status);

    create index invoice_municipality_id_index
       on invoice (municipality_id);

    alter table if exists invoice
       add constraint invoice_unique_invoice_id_constraint unique (invoice_id);
