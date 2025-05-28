CREATE TABLE public."FeaturedDestination_reviews" (
    "FeaturedDestination_id" uuid NOT NULL,
    reviews character varying(255),
    "reviews_ORDER" integer NOT NULL
);

CREATE TABLE public.blacklisted_tokens (
    token character varying(500) NOT NULL,
    "expiryDate" timestamp(6) without time zone
);

CREATE TABLE public.blog_posts (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    title character varying(255) NOT NULL,
    content text NOT NULL,
    trip_id uuid,
    tags text[] DEFAULT '{}'::text[],
    is_public boolean DEFAULT true,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.featured_destinations (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    destination character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    image_url character varying(255) NOT NULL,
    description text NOT NULL,
    days integer NOT NULL,
    avg_rating double precision NOT NULL,
    reviews text[] DEFAULT '{}'::text[],
    is_active boolean DEFAULT true
);

CREATE TABLE public.group_invitations (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    group_trip_id uuid NOT NULL,
    email character varying(255) NOT NULL,
    invited_user_id uuid,
    message text,
    created_by uuid NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    expires_at timestamp with time zone NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    CONSTRAINT group_invitations_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'DECLINED'::character varying, 'EXPIRED'::character varying])::text[])))
);

CREATE TABLE public.group_trip_members (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    group_trip_id uuid NOT NULL,
    user_id uuid NOT NULL,
    joined_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    status character varying(20) DEFAULT 'INVITED'::character varying NOT NULL,
    CONSTRAINT group_trip_members_status_check CHECK (((status)::text = ANY ((ARRAY['INVITED'::character varying, 'ACCEPTED'::character varying, 'REQUESTED'::character varying, 'DECLINED'::character varying])::text[])))
);

CREATE TABLE public.group_trips (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    trip_plan_id uuid NOT NULL,
    created_by_user_id uuid NOT NULL,
    group_name character varying(255) NOT NULL,
    description text,
    status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT group_trips_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'CANCELLED'::character varying, 'COMPLETED'::character varying])::text[])))
);

CREATE TABLE public.password_reset_tokens (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    token text NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    used boolean DEFAULT false
);

CREATE TABLE public.trip_accommodations (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    trip_plan_id uuid NOT NULL,
    day_number integer NOT NULL,
    title character varying(255) NOT NULL,
    address text,
    latitude numeric(10,7),
    longitude numeric(10,7),
    rating numeric(3,1),
    rating_count integer,
    category character varying(100),
    phone_number character varying(50),
    website text
);

CREATE TABLE public.trip_food_options (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    trip_plan_id uuid NOT NULL,
    day_number integer NOT NULL,
    meal_type character varying(50) NOT NULL,
    title character varying(255) NOT NULL,
    address text,
    latitude numeric(10,7),
    longitude numeric(10,7),
    rating numeric(3,1),
    rating_count integer,
    category character varying(100),
    phone_number character varying(50),
    website text,
    cost character varying(20),
    CONSTRAINT trip_food_options_meal_type_check CHECK (((meal_type)::text = ANY ((ARRAY['breakfast'::character varying, 'lunch'::character varying, 'dinner'::character varying, 'snack'::character varying])::text[])))
);

CREATE TABLE public.trip_plans (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    trip_name character varying(255) NOT NULL,
    origin character varying(255) NOT NULL,
    destination character varying(255) NOT NULL,
    days integer NOT NULL,
    budget_total numeric(10,2) NOT NULL,
    budget_breakdown jsonb NOT NULL,
    people integer NOT NULL,
    preferences text,
    trip_type character varying(50) DEFAULT 'Solo'::character varying NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    journey_date date,
    transportation_type character varying(50) DEFAULT 'Bus'::character varying,
    status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT trip_plans_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT trip_plans_transportation_type_check CHECK (((transportation_type)::text = ANY ((ARRAY['Bus'::character varying, 'Launch'::character varying, 'Train'::character varying, 'Plane'::character varying])::text[]))),
    CONSTRAINT trip_plans_trip_type_check CHECK (((trip_type)::text = ANY ((ARRAY['Solo'::character varying, 'Group'::character varying])::text[])))
);

CREATE TABLE public.trip_requests (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    prompt text NOT NULL,
    origin character varying(255) NOT NULL,
    destination character varying(255) NOT NULL,
    days character varying(10) NOT NULL,
    budget character varying(20) NOT NULL,
    people character varying(10) NOT NULL,
    preferences text,
    trip_type character varying(50) DEFAULT 'solo'::character varying NOT NULL,
    journey_date character varying(50) DEFAULT 'today'::character varying NOT NULL,
    travel_class character varying(50) DEFAULT 'economy'::character varying NOT NULL,
    weather_context jsonb,
    generated_plan jsonb NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    feedback text,
    score numeric(3,2),
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT trip_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying])::text[]))),
    CONSTRAINT trip_requests_trip_type_check CHECK (((trip_type)::text = ANY ((ARRAY['solo'::character varying, 'group'::character varying])::text[])))
);

CREATE TABLE public.trip_spot_suggestions (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    trip_plan_id uuid NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    latitude numeric(10,7),
    longitude numeric(10,7),
    recommended_time character varying(100),
    estimated_duration_hours numeric(4,1)
);

CREATE TABLE public.trip_todolist (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    trip_plan_id uuid NOT NULL,
    place_name character varying(255) NOT NULL,
    is_visited boolean DEFAULT false,
    date_added timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    date_visited timestamp with time zone
);

CREATE TABLE public.user_completed_trips (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    trip_plan_id uuid NOT NULL,
    completion_date date DEFAULT CURRENT_DATE NOT NULL,
    rating numeric(3,1),
    feedback text,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.user_profiles (
    user_id uuid NOT NULL,
    bio text,
    profile_picture_url text,
    current_trip_plan_id uuid,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.user_roles (
    user_id uuid NOT NULL,
    role character varying(20) DEFAULT 'USER'::character varying NOT NULL,
    CONSTRAINT user_roles_role_check CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'ADMIN'::character varying])::text[])))
);

CREATE TABLE public.user_sessions (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    refresh_token text NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.users (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    email character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    full_name character varying(255),
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    password character varying(255),
    role character varying(255)
);

COPY public."FeaturedDestination_reviews" ("FeaturedDestination_id", reviews, "reviews_ORDER") FROM stdin;
\.

COPY public.blacklisted_tokens (token, "expiryDate") FROM stdin;
\.

COPY public.blog_posts (id, user_id, title, content, trip_id, tags, is_public, created_at, updated_at) FROM stdin;
\.

COPY public.featured_destinations (id, destination, title, image_url, description, days, avg_rating, reviews, is_active) FROM stdin;
f367d37e-7d59-47b0-beaf-f9c586349f0c	Coxs-Bazar	Sun, Sand & Sea in Coxs-Bazar	/images/coxsbazar.jpg	Experience the longest natural sea beach in the world. Relax under the sun, explore the beach market, and indulge in fresh seafood.	3	4.7	{}	t
8df0dc2c-6f68-40a1-9398-527ac0b0faba	Rangamati	Tranquility Among the Hills in Rangamati	/images/rangamati.jpg	Explore the serene hill districts, take a boat ride on Kaptai Lake, and visit the tribal villages. A perfect getaway for nature lovers.	2	4.5	{}	t
34a9bb68-584f-4481-a68f-e6e15ee26178	Sylhet	Mystic Sylhet – Tea Gardens & Waterfalls	/images/sylhet.jpg	Visit sprawling tea gardens, lush green hills, and enchanting waterfalls. Don’t forget to try the famous Sylheti 7-layer tea!	3	4.6	{}	t
\.

COPY public.group_invitations (id, group_trip_id, email, invited_user_id, message, created_by, created_at, expires_at, status) FROM stdin;
\.

COPY public.group_trip_members (id, group_trip_id, user_id, joined_at, status) FROM stdin;
\.

COPY public.group_trips (id, trip_plan_id, created_by_user_id, group_name, description, status, created_at, updated_at) FROM stdin;
\.

COPY public.password_reset_tokens (id, user_id, token, expires_at, created_at, used) FROM stdin;
\.

COPY public.trip_accommodations (id, trip_plan_id, day_number, title, address, latitude, longitude, rating, rating_count, category, phone_number, website) FROM stdin;
\.

COPY public.trip_food_options (id, trip_plan_id, day_number, meal_type, title, address, latitude, longitude, rating, rating_count, category, phone_number, website, cost) FROM stdin;
\.

COPY public.trip_plans (id, user_id, trip_name, origin, destination, days, budget_total, budget_breakdown, people, preferences, trip_type, start_date, end_date, journey_date, transportation_type, status, created_at, updated_at) FROM stdin;
\.

COPY public.trip_requests (id, user_id, prompt, origin, destination, days, budget, people, preferences, trip_type, journey_date, travel_class, weather_context, generated_plan, status, feedback, score, created_at, updated_at) FROM stdin;
\.

COPY public.trip_spot_suggestions (id, trip_plan_id, name, description, latitude, longitude, recommended_time, estimated_duration_hours) FROM stdin;
\.

COPY public.trip_todolist (id, trip_plan_id, place_name, is_visited, date_added, date_visited) FROM stdin;
\.

COPY public.user_completed_trips (id, user_id, trip_plan_id, completion_date, rating, feedback, created_at) FROM stdin;
\.

COPY public.user_profiles (user_id, bio, profile_picture_url, current_trip_plan_id, created_at, updated_at) FROM stdin;
\.

COPY public.user_roles (user_id, role) FROM stdin;
\.

COPY public.user_sessions (id, user_id, refresh_token, expires_at, created_at) FROM stdin;
\.

COPY public.users (id, email, username, full_name, created_at, password, role) FROM stdin;
f2b40568-78ea-48df-a442-e463cbcb8851	rks@gmail.com	rks	\N	2025-05-25 20:56:05.275056+06	$2a$10$yo0N2U78ng.RAm3iA5xzP.gxfb.XowF2YcNeJ59OGB25/DIISSnIS	USER
9054c01f-cc86-425a-8222-4d951872631e	rakeshdebnath12910@gmail.com	Rakesh Debnath	\N	2025-05-25 20:57:52.224949+06		USER
7e4ef6b9-7a8d-4ef9-9d81-2c9dbaec2f99	abc@gmail.com	' OR 1=1 -- 	\N	2025-05-25 23:21:17.998663+06	$2a$10$pPLQj0z9ZK4bDNidEgo6P.FFwTWGicakXnzT/hwfwOiDkXCP9nQsW	USER
e1f6df6c-df51-40fd-9bb1-269db8fa1588	rifti@gmail.com	rifti	\N	2025-05-26 14:02:34.290185+06	$2a$10$TylZZKDMB0QAtgRkRHsifedgfPI18P8uq4wuUq97/F1pDSP2MRzNe	USER
\.

ALTER TABLE ONLY public."FeaturedDestination_reviews"
    ADD CONSTRAINT "FeaturedDestination_reviews_pkey" PRIMARY KEY ("FeaturedDestination_id", "reviews_ORDER");

ALTER TABLE ONLY public.blacklisted_tokens
    ADD CONSTRAINT blacklisted_tokens_pkey PRIMARY KEY (token);

ALTER TABLE ONLY public.blog_posts
    ADD CONSTRAINT blog_posts_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.featured_destinations
    ADD CONSTRAINT featured_destinations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.group_invitations
    ADD CONSTRAINT group_invitations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.group_trip_members
    ADD CONSTRAINT group_trip_members_group_trip_id_user_id_key UNIQUE (group_trip_id, user_id);

ALTER TABLE ONLY public.group_trip_members
    ADD CONSTRAINT group_trip_members_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.group_trips
    ADD CONSTRAINT group_trips_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_token_key UNIQUE (token);

ALTER TABLE ONLY public.trip_accommodations
    ADD CONSTRAINT trip_accommodations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.trip_food_options
    ADD CONSTRAINT trip_food_options_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.trip_plans
    ADD CONSTRAINT trip_plans_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.trip_requests
    ADD CONSTRAINT trip_requests_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.trip_spot_suggestions
    ADD CONSTRAINT trip_spot_suggestions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.trip_todolist
    ADD CONSTRAINT trip_todolist_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.user_completed_trips
    ADD CONSTRAINT user_completed_trips_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.user_completed_trips
    ADD CONSTRAINT user_completed_trips_user_id_trip_plan_id_key UNIQUE (user_id, trip_plan_id);

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT user_profiles_pkey PRIMARY KEY (user_id);

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id);

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT user_sessions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);

ALTER TABLE ONLY public."FeaturedDestination_reviews"
    ADD CONSTRAINT "FKghmjisvl3uavxodw8hf9cmk0b" FOREIGN KEY ("FeaturedDestination_id") REFERENCES public.featured_destinations(id);

ALTER TABLE ONLY public.blog_posts
    ADD CONSTRAINT blog_posts_trip_id_fkey FOREIGN KEY (trip_id) REFERENCES public.trip_plans(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.blog_posts
    ADD CONSTRAINT blog_posts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.group_invitations
    ADD CONSTRAINT group_invitations_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);

ALTER TABLE ONLY public.group_invitations
    ADD CONSTRAINT group_invitations_group_trip_id_fkey FOREIGN KEY (group_trip_id) REFERENCES public.group_trips(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.group_invitations
    ADD CONSTRAINT group_invitations_invited_user_id_fkey FOREIGN KEY (invited_user_id) REFERENCES public.users(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.group_trip_members
    ADD CONSTRAINT group_trip_members_group_trip_id_fkey FOREIGN KEY (group_trip_id) REFERENCES public.group_trips(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.group_trip_members
    ADD CONSTRAINT group_trip_members_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.group_trips
    ADD CONSTRAINT group_trips_created_by_user_id_fkey FOREIGN KEY (created_by_user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.group_trips
    ADD CONSTRAINT group_trips_trip_plan_id_fkey FOREIGN KEY (trip_plan_id) REFERENCES public.trip_plans(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.trip_accommodations
    ADD CONSTRAINT trip_accommodations_trip_plan_id_fkey FOREIGN KEY (trip_plan_id) REFERENCES public.trip_plans(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.trip_food_options
    ADD CONSTRAINT trip_food_options_trip_plan_id_fkey FOREIGN KEY (trip_plan_id) REFERENCES public.trip_plans(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.trip_plans
    ADD CONSTRAINT trip_plans_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.trip_requests
    ADD CONSTRAINT trip_requests_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.trip_spot_suggestions
    ADD CONSTRAINT trip_spot_suggestions_trip_plan_id_fkey FOREIGN KEY (trip_plan_id) REFERENCES public.trip_plans(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.trip_todolist
    ADD CONSTRAINT trip_todolist_trip_plan_id_fkey FOREIGN KEY (trip_plan_id) REFERENCES public.trip_plans(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.user_completed_trips
    ADD CONSTRAINT user_completed_trips_trip_plan_id_fkey FOREIGN KEY (trip_plan_id) REFERENCES public.trip_plans(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.user_completed_trips
    ADD CONSTRAINT user_completed_trips_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT user_profiles_current_trip_plan_id_fkey FOREIGN KEY (current_trip_plan_id) REFERENCES public.trip_plans(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT user_profiles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT user_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;