create table if not exists execution_facts
(
    finish_time timestamp(6),
    start_time  timestamp(6) not null,
    version     bigint,
    executor_id uuid         not null,
    id          uuid         not null,
    template_id uuid,
    description varchar(500) not null,
    primary key (id)
);

create table if not exists participants
(
    version   bigint,
    id        uuid         not null,
    role_id   uuid         not null,
    email     varchar(320) not null,
    full_name varchar(100) not null,
    password  varchar(60)  not null,
    primary key (id),
    constraint participants_email_key unique (email)
);

create table if not exists roles
(
    id   uuid         not null,
    name varchar(100) not null,
    primary key (id),
    constraint roles_name_key unique (name)
);

create table if not exists templates
(
    version     bigint,
    id          uuid         not null,
    description varchar(500),
    name        varchar(100) not null,
    primary key (id),
    constraint templates_name_key unique (name)
);

create table if not exists testimonies
(
    timestamp         timestamp(6) not null,
    version           bigint,
    execution_fact_id uuid         not null,
    id                uuid         not null,
    witness_id        uuid         not null,
    primary key (id),
    constraint testimonies_witness_id_execution_fact_id_key unique (witness_id, execution_fact_id)
);

alter table if exists execution_facts
    drop constraint if exists FK4liygverkrgerrp7e3fycqxpb;
alter table if exists execution_facts
    drop constraint if exists FK95sw1l5kfuaksy3m7gfinnghf;
alter table if exists participants
    drop constraint if exists FKehp1xmswqmphio8rko6in21ro;
alter table if exists testimonies
    drop constraint if exists FKdfnqkcksm3710g4w0bkylm78t;
alter table if exists testimonies
    drop constraint if exists FKjlv5nvn3isglmpwphuya0kl81;

alter table if exists execution_facts
    add constraint FK4liygverkrgerrp7e3fycqxpb
        foreign key (executor_id)
            references participants;

alter table if exists execution_facts
    add constraint FK95sw1l5kfuaksy3m7gfinnghf
        foreign key (template_id)
            references templates;

alter table if exists participants
    add constraint FKehp1xmswqmphio8rko6in21ro
        foreign key (role_id)
            references roles;

alter table if exists testimonies
    add constraint FKdfnqkcksm3710g4w0bkylm78t
        foreign key (execution_fact_id)
            references execution_facts;

alter table if exists testimonies
    add constraint FKjlv5nvn3isglmpwphuya0kl81
        foreign key (witness_id)
            references participants;

INSERT INTO public.roles (id, name) VALUES ('5bd48aa6-2829-40da-93df-88563633578c', 'ADMIN');
INSERT INTO public.roles (id, name) VALUES ('2eda9278-dfe5-471e-9678-bca1a5ace451', 'PARTICIPANT');
INSERT INTO public.roles (id, name) VALUES ('bd2dc76e-9117-42c3-882f-d440f51a7641', 'GUEST');

INSERT INTO public.participants (id, email, full_name, password, version, role_id) VALUES ('b9f94316-3d4b-4d5d-926e-c5824125d818', 'nazar@gmail.com', 'Nazar', '$2a$10$xYUcDjb2Y3/OjGEMIHKfXuTRMttg7mS3UBB4.QcQasZhfvSXnzgt6', 0, '2eda9278-dfe5-471e-9678-bca1a5ace451');
INSERT INTO public.participants (id, email, full_name, password, version, role_id) VALUES ('cdddb84c-3830-4761-a312-39bb4baa4772', 'orest@gmail.com', 'Orest', '$2a$10$Eqe/nRfjgE8CfdPND5XCYONI.xnw1x/J3ANnXqocjx/uLIC0trYvq', 0, '5bd48aa6-2829-40da-93df-88563633578c');
INSERT INTO public.participants (id, email, full_name, password, version, role_id) VALUES ('2a8163b7-8144-493a-b23e-c96dafd5d5cc', 'anonim@gmail.com', 'Anonim', '$2a$10$ZjzjFOe9qaiUMFhIz7nwAOAiLQq.4CmMZ5f7rJvvyPa9pAk/9FnEe', 0, 'bd2dc76e-9117-42c3-882f-d440f51a7641');

-- create extension IF NOT EXISTS pg_trgm;
-- CREATE INDEX templates_name_fuzzy_search_index ON templates USING GIN (name gin_trgm_ops);

-- grant all on duty_manager.public.testimonies to "";
-- grant all on duty_manager.public.roles to "";
-- grant all on duty_manager.public.participants to ""
-- grant all on duty_manager.public.execution_facts to "";
-- grant all on duty_manager.public.templates to "";


