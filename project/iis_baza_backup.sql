--
-- PostgreSQL database cluster dump
--

\restrict 4msAfmLXpQdD0fBLLZnJ7Yn47wjzh4dP8btuvFXJ4Yc0UkbcfBQpG7mJ4TDOkcz

SET default_transaction_read_only = off;

SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

--
-- Roles
--

CREATE ROLE postgres;
ALTER ROLE postgres WITH SUPERUSER INHERIT CREATEROLE CREATEDB LOGIN REPLICATION BYPASSRLS PASSWORD 'SCRAM-SHA-256$4096:1MlRDr+ZfCNIMSwuzbdvVQ==$lQS3vfFIHU4B15MWd9+inePnNokOoH/O+4o80I7VxvM=:H+3slUYB9IB2g9dNcVwhyFkbFoBz513t/x9LYLmUqf4=';

--
-- User Configurations
--








\unrestrict 4msAfmLXpQdD0fBLLZnJ7Yn47wjzh4dP8btuvFXJ4Yc0UkbcfBQpG7mJ4TDOkcz

--
-- Databases
--

--
-- Database "template1" dump
--

\connect template1

--
-- PostgreSQL database dump
--

\restrict 2gkNfRxqX3qpgylhKJhAmF5D2thZtEjeAWTg9XbwTHmisYRyqBEPmax93XdQ4k9

-- Dumped from database version 16.14
-- Dumped by pg_dump version 16.14

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- PostgreSQL database dump complete
--

\unrestrict 2gkNfRxqX3qpgylhKJhAmF5D2thZtEjeAWTg9XbwTHmisYRyqBEPmax93XdQ4k9

--
-- Database "iis_drug_crm" dump
--

--
-- PostgreSQL database dump
--

\restrict kAs8PChzGcSvKoyZlsbKedoSaPFuRfA2G6NIC6GDTqgBpPGWlMfAPgPSwH0FzmC

-- Dumped from database version 16.14
-- Dumped by pg_dump version 16.14

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: iis_drug_crm; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE iis_drug_crm WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


ALTER DATABASE iis_drug_crm OWNER TO postgres;

\unrestrict kAs8PChzGcSvKoyZlsbKedoSaPFuRfA2G6NIC6GDTqgBpPGWlMfAPgPSwH0FzmC
\connect iis_drug_crm
\restrict kAs8PChzGcSvKoyZlsbKedoSaPFuRfA2G6NIC6GDTqgBpPGWlMfAPgPSwH0FzmC

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: categories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.categories (
    id bigint NOT NULL,
    name character varying(120) NOT NULL,
    description character varying(500),
    status character varying(30) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_by bigint,
    created_at timestamp without time zone NOT NULL,
    updated_by bigint,
    updated_at timestamp without time zone
);


ALTER TABLE public.categories OWNER TO postgres;

--
-- Name: categories_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.categories_id_seq OWNER TO postgres;

--
-- Name: categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.categories_id_seq OWNED BY public.categories.id;


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO postgres;

--
-- Name: ingredients; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ingredients (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    chemical_formula character varying(100),
    type character varying(30) NOT NULL,
    cas character varying(50) NOT NULL,
    status character varying(30) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_by bigint,
    created_at timestamp without time zone NOT NULL,
    updated_by bigint,
    updated_at timestamp without time zone
);


ALTER TABLE public.ingredients OWNER TO postgres;

--
-- Name: ingredients_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ingredients_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ingredients_id_seq OWNER TO postgres;

--
-- Name: ingredients_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ingredients_id_seq OWNED BY public.ingredients.id;


--
-- Name: pricelist_teams; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.pricelist_teams (
    id bigint NOT NULL,
    name character varying(120) NOT NULL,
    leader_id bigint NOT NULL
);


ALTER TABLE public.pricelist_teams OWNER TO postgres;

--
-- Name: pricelist_teams_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.pricelist_teams_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.pricelist_teams_id_seq OWNER TO postgres;

--
-- Name: pricelist_teams_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.pricelist_teams_id_seq OWNED BY public.pricelist_teams.id;


--
-- Name: products; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.products (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description character varying(500),
    subcategory_id bigint NOT NULL,
    therapeutic_area_id bigint NOT NULL,
    status character varying(30) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_by bigint,
    created_at timestamp without time zone NOT NULL,
    updated_by bigint,
    updated_at timestamp without time zone
);


ALTER TABLE public.products OWNER TO postgres;

--
-- Name: products_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.products_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.products_id_seq OWNER TO postgres;

--
-- Name: products_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.products_id_seq OWNED BY public.products.id;


--
-- Name: regions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.regions (
    id bigint NOT NULL,
    name character varying(120) NOT NULL,
    code character varying(20) NOT NULL
);


ALTER TABLE public.regions OWNER TO postgres;

--
-- Name: regions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.regions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.regions_id_seq OWNER TO postgres;

--
-- Name: regions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.regions_id_seq OWNED BY public.regions.id;


--
-- Name: subcategories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.subcategories (
    id bigint NOT NULL,
    category_id bigint NOT NULL,
    name character varying(120) NOT NULL,
    description character varying(500),
    status character varying(30) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_by bigint,
    created_at timestamp without time zone NOT NULL,
    updated_by bigint,
    updated_at timestamp without time zone
);


ALTER TABLE public.subcategories OWNER TO postgres;

--
-- Name: subcategories_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.subcategories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.subcategories_id_seq OWNER TO postgres;

--
-- Name: subcategories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.subcategories_id_seq OWNED BY public.subcategories.id;


--
-- Name: team_members; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.team_members (
    team_id bigint NOT NULL,
    member_id bigint NOT NULL
);


ALTER TABLE public.team_members OWNER TO postgres;

--
-- Name: therapeutic_areas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.therapeutic_areas (
    id bigint NOT NULL,
    name character varying(120) NOT NULL,
    description character varying(500),
    status character varying(30) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_by bigint,
    created_at timestamp without time zone NOT NULL,
    updated_by bigint,
    updated_at timestamp without time zone
);


ALTER TABLE public.therapeutic_areas OWNER TO postgres;

--
-- Name: therapeutic_areas_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.therapeutic_areas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.therapeutic_areas_id_seq OWNER TO postgres;

--
-- Name: therapeutic_areas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.therapeutic_areas_id_seq OWNED BY public.therapeutic_areas.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    username character varying(100) NOT NULL,
    password_hash character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    role character varying(50) NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    has_changed_password boolean DEFAULT false NOT NULL,
    first_name character varying(100),
    last_name character varying(100)
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: variant_version_ingredients; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.variant_version_ingredients (
    id bigint NOT NULL,
    variant_version_id bigint NOT NULL,
    ingredient_id bigint NOT NULL,
    amount numeric(12,4) NOT NULL,
    unit character varying(30) NOT NULL,
    created_by bigint,
    created_at timestamp without time zone NOT NULL,
    updated_by bigint,
    updated_at timestamp without time zone
);


ALTER TABLE public.variant_version_ingredients OWNER TO postgres;

--
-- Name: variant_version_ingredients_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.variant_version_ingredients_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.variant_version_ingredients_id_seq OWNER TO postgres;

--
-- Name: variant_version_ingredients_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.variant_version_ingredients_id_seq OWNED BY public.variant_version_ingredients.id;


--
-- Name: variant_versions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.variant_versions (
    id bigint NOT NULL,
    variant_id bigint NOT NULL,
    version_label character varying(50) NOT NULL,
    description character varying(1000),
    status character varying(30) NOT NULL,
    created_by bigint,
    created_at timestamp without time zone NOT NULL,
    updated_by bigint,
    updated_at timestamp without time zone
);


ALTER TABLE public.variant_versions OWNER TO postgres;

--
-- Name: variant_versions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.variant_versions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.variant_versions_id_seq OWNER TO postgres;

--
-- Name: variant_versions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.variant_versions_id_seq OWNED BY public.variant_versions.id;


--
-- Name: variants; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.variants (
    id bigint NOT NULL,
    product_id bigint NOT NULL,
    form character varying(100) NOT NULL,
    dosage character varying(100) NOT NULL,
    status character varying(30) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_by bigint,
    created_at timestamp without time zone NOT NULL,
    updated_by bigint,
    updated_at timestamp without time zone
);


ALTER TABLE public.variants OWNER TO postgres;

--
-- Name: variants_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.variants_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.variants_id_seq OWNER TO postgres;

--
-- Name: variants_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.variants_id_seq OWNED BY public.variants.id;


--
-- Name: categories id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories ALTER COLUMN id SET DEFAULT nextval('public.categories_id_seq'::regclass);


--
-- Name: ingredients id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ingredients ALTER COLUMN id SET DEFAULT nextval('public.ingredients_id_seq'::regclass);


--
-- Name: pricelist_teams id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pricelist_teams ALTER COLUMN id SET DEFAULT nextval('public.pricelist_teams_id_seq'::regclass);


--
-- Name: products id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products ALTER COLUMN id SET DEFAULT nextval('public.products_id_seq'::regclass);


--
-- Name: regions id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.regions ALTER COLUMN id SET DEFAULT nextval('public.regions_id_seq'::regclass);


--
-- Name: subcategories id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subcategories ALTER COLUMN id SET DEFAULT nextval('public.subcategories_id_seq'::regclass);


--
-- Name: therapeutic_areas id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.therapeutic_areas ALTER COLUMN id SET DEFAULT nextval('public.therapeutic_areas_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: variant_version_ingredients id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variant_version_ingredients ALTER COLUMN id SET DEFAULT nextval('public.variant_version_ingredients_id_seq'::regclass);


--
-- Name: variant_versions id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variant_versions ALTER COLUMN id SET DEFAULT nextval('public.variant_versions_id_seq'::regclass);


--
-- Name: variants id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variants ALTER COLUMN id SET DEFAULT nextval('public.variants_id_seq'::regclass);


--
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.categories (id, name, description, status, created_by, created_at, updated_by, updated_at) FROM stdin;
5	Analgetici	Lekovi za ublažavanje bola.	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
6	Antibiotici	Lekovi za lečenje bakterijskih infekcija.	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	create users	SQL	V1__create_users.sql	-378182768	postgres	2026-05-28 17:10:16.508651	14	t
2	2	add user names	SQL	V2__add_user_names.sql	-1213792547	postgres	2026-05-28 17:10:16.547815	3	t
3	3	create classification tables	SQL	V3__create_classification_tables.sql	-187125717	postgres	2026-05-28 17:10:16.563193	23	t
4	4	create ingredients	SQL	V4__create_ingredients.sql	-123571696	postgres	2026-05-28 17:10:16.594194	7	t
5	5	create products	SQL	V5__create_products.sql	-2002134070	postgres	2026-05-28 17:10:16.612565	8	t
6	6	create regions	SQL	V6__create_regions.sql	1193705536	postgres	2026-05-28 17:10:16.630603	7	t
7	7	create pricelist teams	SQL	V7__create_pricelist_teams.sql	-1501918354	postgres	2026-05-28 17:10:16.645829	10	t
8	8	create variants	SQL	V8__create_variants.sql	1123483660	postgres	2026-05-28 17:10:16.663108	4	t
9	9	create variant versions	SQL	V9__create_variant_versions.sql	1710724056	postgres	2026-05-28 17:10:16.671353	4	t
10	10	create variant version ingredients	SQL	V10__create_variant_version_ingredients.sql	-234547174	postgres	2026-05-28 17:10:16.6797	4	t
\.


--
-- Data for Name: ingredients; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.ingredients (id, name, chemical_formula, type, cas, status, created_by, created_at, updated_by, updated_at) FROM stdin;
4	Ibuprofen	C13H18O2	ACTIVE_SUBSTANCE	15687-27-1	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
5	Paracetamol	C8H9NO2	ACTIVE_SUBSTANCE	103-90-2	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
6	Magnesium stearate	C36H70MgO4	EXCIPIENT	557-04-0	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
7	voda	H2O	EXCIPIENT	000-00-0	ACTIVE	1	2026-05-29 00:07:45.836896	1	2026-05-29 00:07:45.836896
8	MG	MG	EXCIPIENT	976-da	ACTIVE	1	2026-05-29 08:56:04.686648	1	2026-05-29 08:56:04.686648
\.


--
-- Data for Name: pricelist_teams; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.pricelist_teams (id, name, leader_id) FROM stdin;
\.


--
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.products (id, name, description, subcategory_id, therapeutic_area_id, status, created_by, created_at, updated_by, updated_at) FROM stdin;
3	Brufen	Ibuprofen-based anti-inflammatory product.	7	5	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
4	Paracetamol	Pain relief and fever-reducing product.	8	5	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
5	Fervex	Lek za temperaturu	7	5	ACTIVE	1	2026-05-29 00:26:35.932193	1	2026-05-29 00:26:35.932193
\.


--
-- Data for Name: regions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.regions (id, name, code) FROM stdin;
1	Vojvodina	RS
2	Beograd	RS
\.


--
-- Data for Name: subcategories; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.subcategories (id, category_id, name, description, status, created_by, created_at, updated_by, updated_at) FROM stdin;
7	5	Nesteroidni antiinflamatorni lekovi	NSAID lekovi.	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
8	5	Antipiretici	Lekovi za snižavanje temperature.	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
9	6	Beta-laktamski antibiotici	Antibiotici beta-laktamske grupe.	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
\.


--
-- Data for Name: team_members; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.team_members (team_id, member_id) FROM stdin;
\.


--
-- Data for Name: therapeutic_areas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.therapeutic_areas (id, name, description, status, created_by, created_at, updated_by, updated_at) FROM stdin;
5	Reumatologija	Terapijska oblast za inflamatorna i reumatska stanja.	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
6	Infektologija	Terapijska oblast za infektivne bolesti.	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, username, password_hash, email, role, is_active, has_changed_password, first_name, last_name) FROM stdin;
1	admin	$2a$10$u2VReBetxh217YCa54EGneNAyNbB0BOFQcsGSuy6hO5shdDPzWTnC	admin@local.dev	ROLE_ADMIN	t	t	\N	\N
2	ogi	$2a$10$XNggKej.yvDMo4wyFXXt8O.LzE1CRY7oHet4t/dWbFo06plqEIUeG	ogi@gmail.com	ROLE_PORTFOLIO_MANAGER	t	t	ogi	damnj
\.


--
-- Data for Name: variant_version_ingredients; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.variant_version_ingredients (id, variant_version_id, ingredient_id, amount, unit, created_by, created_at, updated_by, updated_at) FROM stdin;
1	6	4	30.0000	mg	2	2026-05-28 17:35:51.336342	\N	\N
2	6	6	5.0000	mg	2	2026-05-28 17:35:51.336342	\N	\N
3	7	4	50.0000	mg	2	2026-05-28 17:35:51.336342	\N	\N
4	8	5	500.0000	mg	2	2026-05-28 17:35:51.336342	\N	\N
5	8	6	10.0000	mg	2	2026-05-28 17:35:51.336342	\N	\N
6	6	5	10.0000	mg	1	2026-05-28 17:42:46.715963	1	2026-05-28 17:42:46.715963
7	7	7	0.0100	g	1	2026-05-29 06:39:45.921289	1	2026-05-29 06:39:45.921289
8	7	6	1.0000	g	1	2026-05-29 08:55:43.770668	1	2026-05-29 08:55:43.770668
\.


--
-- Data for Name: variant_versions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.variant_versions (id, variant_id, version_label, description, status, created_by, created_at, updated_by, updated_at) FROM stdin;
5	4	3.6v	Initial stable formulation.	ARCHIVED	2	2026-05-28 17:35:51.336342	\N	\N
6	4	3.7v	Improved active formulation.	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
7	5	1.0v	Liquid formulation in development.	DEVELOPMENT	2	2026-05-28 17:35:51.336342	\N	\N
8	6	2.1v	Standard paracetamol formulation.	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
9	7	1.0v	prva verzija	DEVELOPMENT	1	2026-05-29 00:42:29.764122	1	2026-05-29 00:42:29.764122
\.


--
-- Data for Name: variants; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.variants (id, product_id, form, dosage, status, created_by, created_at, updated_by, updated_at) FROM stdin;
4	3	TABLET	30mg	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
5	3	LIQUID	50mg	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
6	4	TABLET	500mg	ACTIVE	2	2026-05-28 17:35:51.336342	\N	\N
7	5	TABLET	100mg	ACTIVE	1	2026-05-29 00:31:12.132062	1	2026-05-29 00:31:12.132062
\.


--
-- Name: categories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.categories_id_seq', 6, true);


--
-- Name: ingredients_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ingredients_id_seq', 8, true);


--
-- Name: pricelist_teams_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.pricelist_teams_id_seq', 1, false);


--
-- Name: products_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.products_id_seq', 5, true);


--
-- Name: regions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.regions_id_seq', 2, true);


--
-- Name: subcategories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.subcategories_id_seq', 9, true);


--
-- Name: therapeutic_areas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.therapeutic_areas_id_seq', 6, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 2, true);


--
-- Name: variant_version_ingredients_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.variant_version_ingredients_id_seq', 8, true);


--
-- Name: variant_versions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.variant_versions_id_seq', 9, true);


--
-- Name: variants_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.variants_id_seq', 7, true);


--
-- Name: categories categories_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_name_key UNIQUE (name);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: ingredients ingredients_cas_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ingredients
    ADD CONSTRAINT ingredients_cas_key UNIQUE (cas);


--
-- Name: ingredients ingredients_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ingredients
    ADD CONSTRAINT ingredients_pkey PRIMARY KEY (id);


--
-- Name: team_members pk_team_members; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_members
    ADD CONSTRAINT pk_team_members PRIMARY KEY (team_id, member_id);


--
-- Name: pricelist_teams pricelist_teams_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pricelist_teams
    ADD CONSTRAINT pricelist_teams_name_key UNIQUE (name);


--
-- Name: pricelist_teams pricelist_teams_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pricelist_teams
    ADD CONSTRAINT pricelist_teams_pkey PRIMARY KEY (id);


--
-- Name: products products_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_name_key UNIQUE (name);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: regions regions_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.regions
    ADD CONSTRAINT regions_name_key UNIQUE (name);


--
-- Name: regions regions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.regions
    ADD CONSTRAINT regions_pkey PRIMARY KEY (id);


--
-- Name: subcategories subcategories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subcategories
    ADD CONSTRAINT subcategories_pkey PRIMARY KEY (id);


--
-- Name: therapeutic_areas therapeutic_areas_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.therapeutic_areas
    ADD CONSTRAINT therapeutic_areas_name_key UNIQUE (name);


--
-- Name: therapeutic_areas therapeutic_areas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.therapeutic_areas
    ADD CONSTRAINT therapeutic_areas_pkey PRIMARY KEY (id);


--
-- Name: subcategories uq_subcategories_category_name; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subcategories
    ADD CONSTRAINT uq_subcategories_category_name UNIQUE (category_id, name);


--
-- Name: variant_versions uq_variant_versions_variant_label; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variant_versions
    ADD CONSTRAINT uq_variant_versions_variant_label UNIQUE (variant_id, version_label);


--
-- Name: variants uq_variants_product_form_dosage; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variants
    ADD CONSTRAINT uq_variants_product_form_dosage UNIQUE (product_id, form, dosage);


--
-- Name: variant_version_ingredients uq_vvi_version_ingredient; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variant_version_ingredients
    ADD CONSTRAINT uq_vvi_version_ingredient UNIQUE (variant_version_id, ingredient_id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: variant_version_ingredients variant_version_ingredients_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variant_version_ingredients
    ADD CONSTRAINT variant_version_ingredients_pkey PRIMARY KEY (id);


--
-- Name: variant_versions variant_versions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variant_versions
    ADD CONSTRAINT variant_versions_pkey PRIMARY KEY (id);


--
-- Name: variants variants_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variants
    ADD CONSTRAINT variants_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_team_members_member_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_team_members_member_id ON public.team_members USING btree (member_id);


--
-- Name: pricelist_teams fk_pricelist_teams_leader; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pricelist_teams
    ADD CONSTRAINT fk_pricelist_teams_leader FOREIGN KEY (leader_id) REFERENCES public.users(id) ON DELETE RESTRICT;


--
-- Name: products fk_products_subcategory; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT fk_products_subcategory FOREIGN KEY (subcategory_id) REFERENCES public.subcategories(id) ON DELETE RESTRICT;


--
-- Name: products fk_products_therapeutic_area; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT fk_products_therapeutic_area FOREIGN KEY (therapeutic_area_id) REFERENCES public.therapeutic_areas(id) ON DELETE RESTRICT;


--
-- Name: subcategories fk_subcategories_category; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subcategories
    ADD CONSTRAINT fk_subcategories_category FOREIGN KEY (category_id) REFERENCES public.categories(id) ON DELETE RESTRICT;


--
-- Name: team_members fk_team_members_member; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_members
    ADD CONSTRAINT fk_team_members_member FOREIGN KEY (member_id) REFERENCES public.users(id) ON DELETE RESTRICT;


--
-- Name: team_members fk_team_members_team; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_members
    ADD CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES public.pricelist_teams(id) ON DELETE CASCADE;


--
-- Name: variant_versions fk_variant_versions_variant; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variant_versions
    ADD CONSTRAINT fk_variant_versions_variant FOREIGN KEY (variant_id) REFERENCES public.variants(id) ON DELETE RESTRICT;


--
-- Name: variants fk_variants_product; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variants
    ADD CONSTRAINT fk_variants_product FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE RESTRICT;


--
-- Name: variant_version_ingredients fk_vvi_ingredient; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variant_version_ingredients
    ADD CONSTRAINT fk_vvi_ingredient FOREIGN KEY (ingredient_id) REFERENCES public.ingredients(id) ON DELETE RESTRICT;


--
-- Name: variant_version_ingredients fk_vvi_variant_version; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.variant_version_ingredients
    ADD CONSTRAINT fk_vvi_variant_version FOREIGN KEY (variant_version_id) REFERENCES public.variant_versions(id) ON DELETE RESTRICT;


--
-- PostgreSQL database dump complete
--

\unrestrict kAs8PChzGcSvKoyZlsbKedoSaPFuRfA2G6NIC6GDTqgBpPGWlMfAPgPSwH0FzmC

--
-- Database "postgres" dump
--

\connect postgres

--
-- PostgreSQL database dump
--

\restrict hxI0enROo4XmLbmfNgIbzPpMQHYWoV9bGkEtpYUXPh6bWLteEaZPfLFACUJgfCR

-- Dumped from database version 16.14
-- Dumped by pg_dump version 16.14

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- PostgreSQL database dump complete
--

\unrestrict hxI0enROo4XmLbmfNgIbzPpMQHYWoV9bGkEtpYUXPh6bWLteEaZPfLFACUJgfCR

--
-- PostgreSQL database cluster dump complete
--

