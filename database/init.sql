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
    first_name character varying(255),
    last_name character varying(255),
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);

-- CREATE TABLE public.pending_registrations (
--     id uuid DEFAULT gen_random_uuid() NOT NULL,
--     email character varying(255) NOT NULL UNIQUE,
--     username character varying(255) NOT NULL,
--     password character varying(255) NOT NULL,
--     otp character varying(6) NOT NULL,
--     created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
--     expires_at timestamp with time zone NOT NULL
-- );

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
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    email character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    password character varying(255),
    role character varying(255)
);

CREATE TABLE public.trip_plan (
    id SERIAL PRIMARY KEY,
    user_id uuid REFERENCES users(id) ON DELETE CASCADE,
    trip_plan JSONB NOT NULL,
    status TEXT CHECK (status IN ('upcoming', 'running', 'completed')) NOT NULL DEFAULT 'upcoming',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE travel_cities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE travel_spots (
    id SERIAL PRIMARY KEY,
    city_id INTEGER REFERENCES travel_cities(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    entry_fee INTEGER,
    time_needed INTEGER,
    best_time VARCHAR(100),
    lat DOUBLE PRECISION,
    lon DOUBLE PRECISION,
    image_url TEXT
);

CREATE TABLE travel_hotels (
    id SERIAL PRIMARY KEY,
    spot_id INTEGER REFERENCES travel_spots(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    price_min INTEGER,
    price_max INTEGER,
    rating NUMERIC(2, 1),
    lat DOUBLE PRECISION,
    lon DOUBLE PRECISION,
    contact VARCHAR(20),
    image_url TEXT
);

CREATE TABLE travel_restaurants (
    id SERIAL PRIMARY KEY,
    spot_id INTEGER REFERENCES travel_spots(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    popular_dishes TEXT,
    avg_cost INTEGER,
    lat DOUBLE PRECISION,
    lon DOUBLE PRECISION,
    image_url TEXT
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

COPY public.user_profiles (user_id, bio, profile_picture_url, first_name, last_name, updated_at) FROM stdin;
\.

COPY public.user_roles (user_id, role) FROM stdin;
\.

COPY public.user_sessions (id, user_id, refresh_token, expires_at, created_at) FROM stdin;
\.

COPY public.users (id, email, username, created_at, password, role) FROM stdin;
f2b40568-78ea-48df-a442-e463cbcb8851	rks@gmail.com	rks	2025-05-25 20:56:05.275056+06	$2a$10$yo0N2U78ng.RAm3iA5xzP.gxfb.XowF2YcNeJ59OGB25/DIISSnIS	USER
9054c01f-cc86-425a-8222-4d951872631e	rakeshdebnath12910@gmail.com	Rakesh Debnath	2025-05-25 20:57:52.224949+06		USER
7e4ef6b9-7a8d-4ef9-9d81-2c9dbaec2f99	abc@gmail.com	' OR 1=1 -- 	2025-05-25 23:21:17.998663+06	$2a$10$pPLQj0z9ZK4bDNidEgo6P.FFwTWGicakXnzT/hwfwOiDkXCP9nQsW	USER
e1f6df6c-df51-40fd-9bb1-269db8fa1588	rifti@gmail.com	rifti	2025-05-26 14:02:34.290185+06	$2a$10$TylZZKDMB0QAtgRkRHsifedgfPI18P8uq4wuUq97/F1pDSP2MRzNe	USER
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

-- ALTER TABLE ONLY public.users
--     ADD CONSTRAINT users_pkey PRIMARY KEY (id);

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
    ADD CONSTRAINT user_profiles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT user_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

-- ALTER TABLE ONLY travel_cities
--     ADD CONSTRAINT travel_cities_pkey PRIMARY KEY (id);

-- ALTER TABLE ONLY travel_spots
--     ADD CONSTRAINT travel_spots_pkey PRIMARY KEY (id);

-- ALTER TABLE ONLY travel_hotels
--     ADD CONSTRAINT travel_hotels_pkey PRIMARY KEY (id);

-- ALTER TABLE ONLY travel_restaurants
--     ADD CONSTRAINT travel_restaurants_pkey PRIMARY KEY (id);

-- Travel data for destinations
-- Insert cities into travel_cities table
INSERT INTO travel_cities (name, description) VALUES 
('Sylhet', 'A city in northeastern Bangladesh known for its tea gardens, hills, and Sufi shrines.'),
('Rangamati', 'A scenic hill district in southeastern Bangladesh, famous for its lakes, tribal culture, and natural beauty.');

-- Insert Sylhet spots
INSERT INTO travel_spots (city_id, name, description, entry_fee, time_needed, best_time, lat, lon, image_url)
VALUES 
(1, 'Ratargul Swamp Forest', 'Freshwater swamp forest with boat rides and wildlife.', 50, 3, 'Morning, Dry Season', 25.0025, 91.9966, 'https://cdn.example.com/images/ratargul.jpg'),
(1, 'Jaflong', 'Scenic area with hills, stones, and Dawki River near the border.', 100, 4, 'All Day', 25.1652, 92.0178, 'https://cdn.example.com/images/jaflong.jpg'),
(1, 'Shahjalal Dargah', 'Famous Sufi shrine and pilgrimage site.', 0, 1, 'Evening', 24.8919, 91.8710, 'https://cdn.example.com/images/shahjalal_dargah.jpg'),
(1, 'Lalakhal', 'Stunning blue-water river site with boat rides.', 150, 3, 'Morning', 25.1081, 92.0021, 'https://cdn.example.com/images/lalakhal.jpg');

-- Insert Rangamati spots
INSERT INTO travel_spots (city_id, name, description, entry_fee, time_needed, best_time, lat, lon, image_url)
VALUES 
(2, 'Kaptai Lake', 'The largest man-made lake in Bangladesh, perfect for scenic boat rides and peaceful relaxation.', 50, 3, 'Morning and evening', 22.6500, 92.1833, 'https://cdn.example.com/images/kaptai_lake.jpg'),
(2, 'Hanging Bridge', 'A 335-feet-long suspension bridge over the lake, iconic to Rangamati tourism.', 20, 1, 'Daytime', 22.6382, 92.1813, 'https://cdn.example.com/images/hanging_bridge.jpg'),
(2, 'Sajek Valley', 'A breathtaking hill station known as the "Queen of Hills" in Bangladesh, famous for misty mountains, waterfalls, and tribal culture.', 0, 8, 'October to April (dry season)', 23.2026, 91.7637, 'https://cdn.example.com/images/sajek_valley.jpg'),
(2, 'Rajban Vihara', 'A serene Buddhist monastery surrounded by hills, offering spiritual calm and beautiful architecture.', 0, 2, 'Morning or afternoon', 22.6145, 92.1848, 'https://cdn.example.com/images/rajban_vihara.jpg');

-- Insert hotels for Sylhet spots
INSERT INTO travel_hotels (spot_id, name, price_min, price_max, rating, lat, lon, contact, image_url) VALUES
-- Hotels for Ratargul (spot_id = 1)
(1, 'Swamp View Resort', 2000, 3500, 4.2, 25.0011, 91.9955, '01712345678', 'https://cdn.example.com/images/hotel_swamp_view.jpg'),
(1, 'Green Valley Cottage', 1500, 2500, 4.0, 25.0040, 91.9978, '01787654321', 'https://cdn.example.com/images/green_valley.jpg'),
-- Hotels for Jaflong (spot_id = 2)
(2, 'Jaflong View Hotel', 1800, 3000, 4.1, 25.1666, 92.0185, '01711224455', 'https://cdn.example.com/images/jaflong_view.jpg'),
(2, 'Paharika Inn', 1500, 2500, 3.8, 25.1641, 92.0169, '01799887766', 'https://cdn.example.com/images/paharika_inn.jpg'),
-- Hotels for Shahjalal Dargah (spot_id = 3)
(3, 'Hotel Metro International', 2000, 4000, 4.0, 24.8951, 91.8689, '01744556677', 'https://cdn.example.com/images/hotel_metro.jpg'),
(3, 'Hotel Star Pacific', 3500, 6000, 4.4, 24.8912, 91.8708, '01755667788', 'https://cdn.example.com/images/star_pacific.jpg'),
-- Hotels for Lalakhal (spot_id = 4)
(4, 'Lalakhal Eco Resort', 2500, 4500, 4.3, 25.1075, 92.0015, '01722998844', 'https://cdn.example.com/images/lalakhal_resort.jpg'),
(4, 'Nature Nest', 1800, 3000, 3.9, 25.1092, 92.0030, '01733445566', 'https://cdn.example.com/images/nature_nest.jpg');

-- Insert hotels for Rangamati spots
INSERT INTO travel_hotels (spot_id, name, price_min, price_max, rating, lat, lon, contact, image_url) VALUES
-- Hotels for Kaptai Lake (spot_id = 5)
(5, 'Parjatan Holiday Complex', 2000, 4000, 4.3, 22.6550, 92.1800, '01811223344', 'https://cdn.example.com/images/parjatan_rangamati.jpg'),
(5, 'Lake View Resort', 1800, 3000, 4.0, 22.6525, 92.1840, '01899887766', 'https://cdn.example.com/images/lake_view_rangamati.jpg'),
-- Hotels for Hanging Bridge (spot_id = 6)
(6, 'Bridge Side Lodge', 1500, 2500, 3.9, 22.6379, 92.1808, '01711223355', 'https://cdn.example.com/images/bridge_lodge.jpg'),
(6, 'Rangamati Garden Inn', 2200, 3800, 4.2, 22.6390, 92.1825, '01733446688', 'https://cdn.example.com/images/garden_inn.jpg'),
-- Hotels for Sajek Valley (spot_id = 7)
(7, 'Sajek Hill Resort', 2500, 4500, 4.1, 23.2035, 91.7642, '01722334455', 'https://cdn.example.com/images/sajek_resort.jpg'),
(7, 'Hilltop Guest House', 1800, 3200, 3.9, 23.2010, 91.7630, '01799887744', 'https://cdn.example.com/images/hilltop_guest.jpg'),
-- Hotels for Rajban Vihara (spot_id = 8)
(8, 'Vihara View Hotel', 1500, 2800, 4.0, 22.6150, 92.1852, '01755668899', 'https://cdn.example.com/images/vihara_view.jpg'),
(8, 'Peaceful Stay Inn', 1300, 2500, 3.7, 22.6138, 92.1840, '01777889900', 'https://cdn.example.com/images/peaceful_stay.jpg');

-- Insert restaurants for Sylhet spots
INSERT INTO travel_restaurants (spot_id, name, popular_dishes, avg_cost, lat, lon, image_url) VALUES
-- Restaurants for Ratargul (spot_id = 1)
(1, 'Swamp Side Dine', 'Grilled fish, Rice & dal, Fresh lemonade', 250, 25.0021, 91.9961, 'https://cdn.example.com/images/swamp_dine.jpg'),
(1, 'Forest Bite', 'Chicken curry, Vegetable bharta', 200, 25.0032, 91.9950, 'https://cdn.example.com/images/forest_bite.jpg'),
-- Restaurants for Jaflong (spot_id = 2)
(2, 'Border Grill', 'Grilled chicken, Paratha, Shatkora curry', 300, 25.1660, 92.0173, 'https://cdn.example.com/images/border_grill.jpg'),
(2, 'Jaflong Foods', 'Polao, Beef curry, Tea', 220, 25.1655, 92.0165, 'https://cdn.example.com/images/jaflong_foods.jpg'),
-- Restaurants for Shahjalal Dargah (spot_id = 3)
(3, 'Woondaal King Kebab', 'Kebab, Chicken roast, Borhani', 400, 24.8943, 91.8703, 'https://cdn.example.com/images/woondaal.jpg'),
(3, 'Kutum Bari', 'Beef curry, Rice, Shorisha Ilish', 300, 24.8871, 91.8752, 'https://cdn.example.com/images/kutum_bari.jpg'),
-- Restaurants for Lalakhal (spot_id = 4)
(4, 'River Breeze Dine', 'Fresh fish, Mustard curry, Rice', 270, 25.1080, 92.0017, 'https://cdn.example.com/images/river_breeze.jpg'),
(4, 'Blue Water Eatery', 'Polao, Chicken roast', 240, 25.1078, 92.0022, 'https://cdn.example.com/images/blue_water.jpg');

-- Insert restaurants for Rangamati spots
INSERT INTO travel_restaurants (spot_id, name, popular_dishes, avg_cost, lat, lon, image_url) VALUES
-- Restaurants for Kaptai Lake (spot_id = 5)
(5, 'Lake Breeze Restaurant', 'Grilled fish, Bamboo chicken, Rice', 300, 22.6511, 92.1831, 'https://cdn.example.com/images/lake_breeze.jpg'),
(5, 'Tribal Food Corner', 'Bamboo beef, Sticky rice, Local herbs', 250, 22.6505, 92.1845, 'https://cdn.example.com/images/tribal_food.jpg'),
-- Restaurants for Hanging Bridge (spot_id = 6)
(6, 'Bridge View Café', 'Fish fry, Paratha, Coconut water', 200, 22.6384, 92.1811, 'https://cdn.example.com/images/bridge_cafe.jpg'),
(6, 'Hillside Bites', 'Chicken curry, Lentils, Steamed rice', 180, 22.6395, 92.1828, 'https://cdn.example.com/images/hillside_bites.jpg'),
-- Restaurants for Sajek Valley (spot_id = 7)
(7, 'Valley View Café', 'Local tribal dishes, Bamboo chicken, Herbal tea', 300, 23.2020, 91.7635, 'https://cdn.example.com/images/valley_cafe.jpg'),
(7, 'Mountain Delight', 'Grilled fish, Rice, Vegetable curry', 250, 23.2030, 91.7640, 'https://cdn.example.com/images/mountain_delight.jpg'),
-- Restaurants for Rajban Vihara (spot_id = 8)
(8, 'Monastery Café', 'Veggie curry, Rice, Tea', 180, 22.6147, 92.1847, 'https://cdn.example.com/images/monastery_cafe.jpg'),
(8, 'Tranquil Bites', 'Local snacks, Herbal drinks', 150, 22.6155, 92.1850, 'https://cdn.example.com/images/tranquil_bites.jpg');