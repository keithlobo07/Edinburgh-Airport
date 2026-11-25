package io.github.keithlobo07.edinburghairport.data

import io.github.keithlobo07.edinburghairport.model.NavigationStep
import io.github.keithlobo07.edinburghairport.model.PlaceCategory
import io.github.keithlobo07.edinburghairport.model.StepIcon
import io.github.keithlobo07.edinburghairport.model.TerminalLevel
import io.github.keithlobo07.edinburghairport.model.TerminalPlace
import io.github.keithlobo07.edinburghairport.model.WayfindingRoute

object TerminalMapData {

    val places: List<TerminalPlace> = listOf(
        // =========================================================================
        // === LEVEL 1: OFFICIAL AIRSIDE DEPARTURES DIRECTORY (ALL GATES & SHOPS) ===
        // =========================================================================

        // --- SECURITY CHECKPOINT ---
        TerminalPlace(
            id = "sec-central",
            name = "Security Central Checkpoint",
            category = PlaceCategory.SECURITY,
            level = TerminalLevel.LEVEL_1,
            x = 0.36f,
            y = 0.58f,
            subtitle = "Main Screening & FastTrack",
            description = "Central security search area equipped with high-tech 3D CT bag scanners. Liquids up to 2L stay inside your bag.",
            operatingHours = "Open 04:00 - 22:30",
            walkingTimeMin = 1,
            walkingDistanceMeters = 50
        ),

        // --- WORLD DUTY FREE ---
        TerminalPlace(
            id = "duty-free-main",
            name = "World Duty Free",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.38f,
            y = 0.40f,
            subtitle = "Main Concourse Walkthrough Store",
            description = "Iconic walkthrough duty-free store with Scottish malt whiskies, luxury perfumes, cosmetics, confectionery, and designer sunglasses.",
            operatingHours = "Open 04:00 - last flight",
            walkingTimeMin = 2,
            walkingDistanceMeters = 80
        ),
        TerminalPlace(
            id = "duty-free-express",
            name = "World Duty Free Express",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.62f,
            y = 0.28f,
            subtitle = "Express Concourse Kiosk",
            description = "Quick-grab top 20 fragrances, tobacco, spirits, and travel essentials.",
            operatingHours = "Open 05:00 - 21:00",
            walkingTimeMin = 3,
            walkingDistanceMeters = 150
        ),

        // --- GATES 1 TO 30 (EVERY SINGLE GATE VISIBLE & NUMBERED) ---
        TerminalPlace(
            id = "gate-1",
            name = "Gate 1",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.03f,
            y = 0.46f,
            subtitle = "West Wing Pier",
            description = "Regional and domestic boarding gate with dedicated waiting area and toilets.",
            gateNumber = 1,
            walkingTimeMin = 5,
            walkingDistanceMeters = 240
        ),
        TerminalPlace(
            id = "gate-2",
            name = "Gate 2",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.03f,
            y = 0.53f,
            subtitle = "West Wing Pier",
            description = "Short-haul departure gate with ramp access.",
            gateNumber = 2,
            walkingTimeMin = 5,
            walkingDistanceMeters = 260
        ),
        TerminalPlace(
            id = "gate-3",
            name = "Gate 3",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.03f,
            y = 0.60f,
            subtitle = "West Wing Pier",
            description = "Regional departure gate.",
            gateNumber = 3,
            walkingTimeMin = 5,
            walkingDistanceMeters = 280
        ),
        TerminalPlace(
            id = "gate-4",
            name = "Gate 4",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.05f,
            y = 0.36f,
            subtitle = "West Apron • Beside Escape Lounge",
            description = "Boarding gate for domestic and European departures.",
            gateNumber = 4,
            walkingTimeMin = 4,
            walkingDistanceMeters = 210
        ),
        TerminalPlace(
            id = "gate-5",
            name = "Gate 5",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.05f,
            y = 0.30f,
            subtitle = "West Apron",
            description = "Boarding gate with airfield views.",
            gateNumber = 5,
            walkingTimeMin = 4,
            walkingDistanceMeters = 220
        ),
        TerminalPlace(
            id = "gate-6",
            name = "Gate 6",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.14f,
            y = 0.06f,
            subtitle = "North-West Apron • Beside All Bar One",
            description = "Spacious boarding lounge for domestic and short-haul flights.",
            gateNumber = 6,
            walkingTimeMin = 4,
            walkingDistanceMeters = 200
        ),
        TerminalPlace(
            id = "gate-7",
            name = "Gate 7",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.27f,
            y = 0.06f,
            subtitle = "North Apron • Beside Pret & Nero",
            description = "Direct jet-bridge boarding gate for European destinations.",
            gateNumber = 7,
            walkingTimeMin = 3,
            walkingDistanceMeters = 150
        ),
        TerminalPlace(
            id = "gate-8",
            name = "Gate 8",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.30f,
            y = 0.06f,
            subtitle = "North Apron",
            description = "Boarding gate for European flights.",
            gateNumber = 8,
            walkingTimeMin = 3,
            walkingDistanceMeters = 160
        ),
        TerminalPlace(
            id = "gate-9",
            name = "Gate 9",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.34f,
            y = 0.06f,
            subtitle = "North Apron • Beside WHSmith",
            description = "Main concourse boarding gate.",
            gateNumber = 9,
            walkingTimeMin = 3,
            walkingDistanceMeters = 160
        ),
        TerminalPlace(
            id = "gate-10",
            name = "Gate 10",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.52f,
            y = 0.06f,
            subtitle = "North Concourse • Beside Boots & JD",
            description = "Airfield boarding gate with digital flight displays.",
            gateNumber = 10,
            walkingTimeMin = 4,
            walkingDistanceMeters = 180
        ),
        TerminalPlace(
            id = "gate-11",
            name = "Gate 11",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.61f,
            y = 0.06f,
            subtitle = "North Concourse • Beside Forth & Main",
            description = "Boarding gate serving European flag carriers.",
            gateNumber = 11,
            walkingTimeMin = 4,
            walkingDistanceMeters = 200
        ),
        TerminalPlace(
            id = "gate-12",
            name = "Gate 12",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.74f,
            y = 0.34f,
            subtitle = "East Concourse • Beside Burger King",
            description = "Boarding gate connecting Central Concourse to the East Wing.",
            gateNumber = 12,
            walkingTimeMin = 4,
            walkingDistanceMeters = 210
        ),
        TerminalPlace(
            id = "gate-13",
            name = "Gate 13",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.94f,
            y = 0.38f,
            subtitle = "East Pier • Beside Pret East",
            description = "Boarding gate for British Airways and European services.",
            gateNumber = 13,
            walkingTimeMin = 5,
            walkingDistanceMeters = 270
        ),
        TerminalPlace(
            id = "gate-14",
            name = "Gate 14",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.94f,
            y = 0.42f,
            subtitle = "East Pier • Beside Aspire Lounge",
            description = "Direct jet-bridge departure gate for London Heathrow and major hubs.",
            gateNumber = 14,
            walkingTimeMin = 5,
            walkingDistanceMeters = 280
        ),
        TerminalPlace(
            id = "gate-15",
            name = "Gate 15",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.94f,
            y = 0.56f,
            subtitle = "East Pier",
            description = "Boarding gate for international departures.",
            gateNumber = 15,
            walkingTimeMin = 6,
            walkingDistanceMeters = 310
        ),
        TerminalPlace(
            id = "gate-16",
            name = "Gate 16",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.94f,
            y = 0.60f,
            subtitle = "East Pier • Beside Icons Sports Bar",
            description = "Wide-body capable boarding gate.",
            gateNumber = 16,
            walkingTimeMin = 6,
            walkingDistanceMeters = 330
        ),
        TerminalPlace(
            id = "gate-17",
            name = "Gate 17",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.79f,
            y = 0.74f,
            subtitle = "South-East Pier",
            description = "Dedicated South-East Pier boarding gate with comfortable seating.",
            gateNumber = 17,
            walkingTimeMin = 6,
            walkingDistanceMeters = 350
        ),
        TerminalPlace(
            id = "gate-18",
            name = "Gate 18",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.84f,
            y = 0.74f,
            subtitle = "South-East Pier",
            description = "Boarding gate with ramp access.",
            gateNumber = 18,
            walkingTimeMin = 6,
            walkingDistanceMeters = 360
        ),
        TerminalPlace(
            id = "gate-19",
            name = "Gate 19",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.79f,
            y = 0.79f,
            subtitle = "South-East Pier",
            description = "European departures gate.",
            gateNumber = 19,
            walkingTimeMin = 7,
            walkingDistanceMeters = 380
        ),
        TerminalPlace(
            id = "gate-20",
            name = "Gate 20",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.84f,
            y = 0.79f,
            subtitle = "South-East Pier",
            description = "Boarding gate with digital announcements.",
            gateNumber = 20,
            walkingTimeMin = 7,
            walkingDistanceMeters = 390
        ),
        TerminalPlace(
            id = "gate-21",
            name = "Gate 21",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.79f,
            y = 0.84f,
            subtitle = "South-East Pier",
            description = "Mid-pier departure gate.",
            gateNumber = 21,
            walkingTimeMin = 7,
            walkingDistanceMeters = 410
        ),
        TerminalPlace(
            id = "gate-22",
            name = "Gate 22",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.84f,
            y = 0.84f,
            subtitle = "South-East Pier",
            description = "Boarding gate with charging stations.",
            gateNumber = 22,
            walkingTimeMin = 7,
            walkingDistanceMeters = 420
        ),
        TerminalPlace(
            id = "gate-23",
            name = "Gate 23",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.79f,
            y = 0.89f,
            subtitle = "South-East Pier",
            description = "Boarding gate serving Ryanair and easyJet.",
            gateNumber = 23,
            walkingTimeMin = 8,
            walkingDistanceMeters = 450
        ),
        TerminalPlace(
            id = "gate-24",
            name = "Gate 24",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.84f,
            y = 0.89f,
            subtitle = "South-East Pier",
            description = "Boarding gate with airfield views.",
            gateNumber = 24,
            walkingTimeMin = 8,
            walkingDistanceMeters = 460
        ),
        TerminalPlace(
            id = "gate-25",
            name = "Gate 25",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.79f,
            y = 0.94f,
            subtitle = "South-East Pier End",
            description = "Boarding gate with spacious queuing zone and seating.",
            gateNumber = 25,
            walkingTimeMin = 9,
            walkingDistanceMeters = 500
        ),
        TerminalPlace(
            id = "gate-26",
            name = "Gate 26",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.84f,
            y = 0.94f,
            subtitle = "South-East Pier End",
            description = "Boarding gate for holiday flights.",
            gateNumber = 26,
            walkingTimeMin = 9,
            walkingDistanceMeters = 510
        ),
        TerminalPlace(
            id = "gate-27",
            name = "Gate 27",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.79f,
            y = 0.98f,
            subtitle = "South-East Pier End",
            description = "Short-haul departure gate.",
            gateNumber = 27,
            walkingTimeMin = 9,
            walkingDistanceMeters = 530
        ),
        TerminalPlace(
            id = "gate-28",
            name = "Gate 28",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.84f,
            y = 0.98f,
            subtitle = "South-East Pier End",
            description = "Boarding gate with ramp access.",
            gateNumber = 28,
            walkingTimeMin = 9,
            walkingDistanceMeters = 540
        ),
        TerminalPlace(
            id = "gate-29",
            name = "Gate 29",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.80f,
            y = 1.02f,
            subtitle = "South-East Pier Extension Tip",
            description = "Terminal gate located at the southern tip of the pier extension.",
            gateNumber = 29,
            walkingTimeMin = 10,
            walkingDistanceMeters = 570
        ),
        TerminalPlace(
            id = "gate-30",
            name = "Gate 30",
            category = PlaceCategory.GATES,
            level = TerminalLevel.LEVEL_1,
            x = 0.84f,
            y = 1.02f,
            subtitle = "South-East Pier Extension Tip",
            description = "Terminal gate located at the southern tip of the pier extension.",
            gateNumber = 30,
            walkingTimeMin = 10,
            walkingDistanceMeters = 580
        ),

        // --- DINING & RESTAURANTS (COMPLETE OFFICIAL DIRECTORY) ---
        TerminalPlace(
            id = "all-bar-one-west",
            name = "All Bar One (West)",
            category = PlaceCategory.DINING,
            level = TerminalLevel.LEVEL_1,
            x = 0.11f,
            y = 0.16f,
            subtitle = "Stylish Wine Bar, Cocktails & Tapas",
            description = "Vibrant bar and brasserie near Gate 6 with extensive wine list, sharing plates, and Scottish craft beers.",
            operatingHours = "Open 04:30 - 21:00",
            walkingTimeMin = 4,
            walkingDistanceMeters = 190
        ),
        TerminalPlace(
            id = "giraffe",
            name = "Giraffe World Kitchen",
            category = PlaceCategory.DINING,
            level = TerminalLevel.LEVEL_1,
            x = 0.18f,
            y = 0.35f,
            subtitle = "Global Cuisine & Family Dining",
            description = "Freshly cooked international dishes, hearty burgers, brunch bowls, smoothies, and a dedicated kids menu.",
            operatingHours = "Open 04:30 - 20:30",
            walkingTimeMin = 3,
            walkingDistanceMeters = 140
        ),
        TerminalPlace(
            id = "pret-west",
            name = "Pret A Manger (West)",
            category = PlaceCategory.DINING,
            level = TerminalLevel.LEVEL_1,
            x = 0.20f,
            y = 0.16f,
            subtitle = "Organic Coffee, Sandwiches & Salads",
            description = "Freshly made baguettes, hot wraps, fruit pots, and organic barista coffee.",
            operatingHours = "Open 04:00 - last flight",
            walkingTimeMin = 3,
            walkingDistanceMeters = 130
        ),
        TerminalPlace(
            id = "caffe-nero-west",
            name = "Caffè Nero (West)",
            category = PlaceCategory.DINING,
            level = TerminalLevel.LEVEL_1,
            x = 0.25f,
            y = 0.16f,
            subtitle = "Award-winning Italian Coffee",
            description = "Premium espresso coffee, sourdough toasties, and Italian pastries.",
            operatingHours = "Open 04:30 - 20:30",
            walkingTimeMin = 2,
            walkingDistanceMeters = 110
        ),
        TerminalPlace(
            id = "fever-tree",
            name = "Fever-Tree Bar & Cafe",
            category = PlaceCategory.DINING,
            level = TerminalLevel.LEVEL_1,
            x = 0.31f,
            y = 0.20f,
            subtitle = "Signature G&Ts, Spritzes & Small Plates",
            description = "Curated cocktail bar highlighting Scottish spirits paired with world-renowned Fever-Tree mixers.",
            operatingHours = "Open 04:30 - 21:30",
            walkingTimeMin = 2,
            walkingDistanceMeters = 90
        ),
        TerminalPlace(
            id = "starbucks-central",
            name = "Starbucks Coffee",
            category = PlaceCategory.DINING,
            level = TerminalLevel.LEVEL_1,
            x = 0.52f,
            y = 0.20f,
            subtitle = "Espresso, Frappuccinos & Fresh Pastries",
            description = "Handcrafted espresso beverages, iced drinks, breakfast sandwiches, and quick snacks.",
            operatingHours = "Open 04:00 - 22:00",
            walkingTimeMin = 3,
            walkingDistanceMeters = 130
        ),
        TerminalPlace(
            id = "wetherspoon",
            name = "The Sir Walter Scott (JD Wetherspoon)",
            category = PlaceCategory.DINING,
            level = TerminalLevel.LEVEL_1,
            x = 0.56f,
            y = 0.24f,
            subtitle = "Traditional Pub Grub & Scottish Ales",
            description = "Spacious airport pub offering full Scottish breakfasts, burgers, fish & chips, and cask ales at high-street prices.",
            operatingHours = "Open 04:00 - last flight",
            walkingTimeMin = 3,
            walkingDistanceMeters = 140
        ),
        TerminalPlace(
            id = "forth-and-main",
            name = "Forth & Main Diner & Bar",
            category = PlaceCategory.DINING,
            level = TerminalLevel.LEVEL_1,
            x = 0.67f,
            y = 0.15f,
            subtitle = "Craft Burgers, Steaks & Local Brews",
            description = "Airfield-facing restaurant with hearty Scottish grill classics, craft beers, and booth seating.",
            operatingHours = "Open 04:30 - 21:00",
            walkingTimeMin = 4,
            walkingDistanceMeters = 180
        ),
        TerminalPlace(
            id = "burger-king",
            name = "Burger King",
            category = PlaceCategory.DINING,
            level = TerminalLevel.LEVEL_1,
            x = 0.68f,
            y = 0.26f,
            subtitle = "Flame-grilled Burgers & Fries",
            description = "Fast flame-grilled Whoppers, plant-based burgers, chicken sandwiches, and crispy fries.",
            operatingHours = "Open 04:30 - 22:00",
            walkingTimeMin = 4,
            walkingDistanceMeters = 190
        ),
        TerminalPlace(
            id = "seoul-bird",
            name = "Seoul Bird",
            category = PlaceCategory.DINING,
            level = TerminalLevel.LEVEL_1,
            x = 0.74f,
            y = 0.50f,
            subtitle = "Korean Crispy Fried Chicken & Bowls",
            description = "Crispy double-fried Korean fried chicken burgers, kimchi rice bowls, and loaded tater tots by Judy Joo.",
            operatingHours = "Open 05:00 - 21:00",
            walkingTimeMin = 5,
            walkingDistanceMeters = 240
        ),
        TerminalPlace(
            id = "pret-east",
            name = "Pret A Manger (East)",
            category = PlaceCategory.DINING,
            level = TerminalLevel.LEVEL_1,
            x = 0.81f,
            y = 0.40f,
            subtitle = "Organic Coffee & Fresh Sandwiches",
            description = "Second Pret location convenient for gates 13 through 16.",
            operatingHours = "Open 04:30 - 20:30",
            walkingTimeMin = 5,
            walkingDistanceMeters = 250
        ),
        TerminalPlace(
            id = "icons-sports-bar",
            name = "Icons Sports Bar & Kitchen",
            category = PlaceCategory.DINING,
            level = TerminalLevel.LEVEL_1,
            x = 0.81f,
            y = 0.54f,
            subtitle = "Live Sports, Craft Beers & Loaded Nachos",
            description = "Premier airport sports lounge with massive 4K video walls showing live Premier League, rugby, and golf.",
            operatingHours = "Open 05:00 - last flight",
            walkingTimeMin = 5,
            walkingDistanceMeters = 270
        ),
        TerminalPlace(
            id = "all-bar-one-east",
            name = "All Bar One (East)",
            category = PlaceCategory.DINING,
            level = TerminalLevel.LEVEL_1,
            x = 0.77f,
            y = 0.63f,
            subtitle = "Wine, Small Plates & Cocktails",
            description = "Second All Bar One situated by the walkway to gates 17-30.",
            operatingHours = "Open 05:00 - 21:00",
            walkingTimeMin = 6,
            walkingDistanceMeters = 300
        ),
        TerminalPlace(
            id = "caffe-nero-pier",
            name = "Caffè Nero (South-East Pier)",
            category = PlaceCategory.DINING,
            level = TerminalLevel.LEVEL_1,
            x = 0.82f,
            y = 0.74f,
            subtitle = "Coffee & Snacks for Gates 17-30",
            description = "Convenient coffee counter located right inside the pier extension.",
            operatingHours = "Open 04:30 - 20:30",
            walkingTimeMin = 6,
            walkingDistanceMeters = 340
        ),

        // --- SHOPS & RETAIL (COMPLETE OFFICIAL DIRECTORY) ---
        TerminalPlace(
            id = "boots",
            name = "Boots Pharmacy",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.43f,
            y = 0.13f,
            subtitle = "Pharmacy, Meal Deals & Travel Essentials",
            description = "Prescription and travel pharmacy, toiletries, suntan lotion, and famous £4.99 Boots Meal Deal.",
            operatingHours = "Open 04:00 - last flight",
            walkingTimeMin = 2,
            walkingDistanceMeters = 90
        ),
        TerminalPlace(
            id = "whsmith-central",
            name = "WHSmith (Central)",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.38f,
            y = 0.13f,
            subtitle = "Books, Magazines & Snacks",
            description = "Bestsellers, Scottish travel guides, confectionery, cold drinks, and tech essentials.",
            operatingHours = "Open 04:00 - last flight",
            walkingTimeMin = 2,
            walkingDistanceMeters = 80
        ),
        TerminalPlace(
            id = "whsmith-bookshop",
            name = "WHSmith Bookshop",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.24f,
            y = 0.30f,
            subtitle = "Specialist Books & Reading",
            description = "Curated airport bookstore with fiction, non-fiction, kids books, and Scottish history.",
            operatingHours = "Open 04:30 - 20:30",
            walkingTimeMin = 2,
            walkingDistanceMeters = 100
        ),
        TerminalPlace(
            id = "whsmith-east",
            name = "WHSmith (East Pier)",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.86f,
            y = 0.46f,
            subtitle = "Travel Convenience for Gates 13-16",
            description = "Grab-and-go water, newspapers, confectionery, and headphones.",
            operatingHours = "Open 04:30 - 21:00",
            walkingTimeMin = 5,
            walkingDistanceMeters = 260
        ),
        TerminalPlace(
            id = "whsmith-pier",
            name = "WHSmith (Gates 17-30)",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.80f,
            y = 0.74f,
            subtitle = "Pier Extension Kiosk",
            description = "Water bottles, newspapers, and snacks for south pier gates.",
            operatingHours = "Open 04:30 - 20:30",
            walkingTimeMin = 6,
            walkingDistanceMeters = 340
        ),
        TerminalPlace(
            id = "jd-sports",
            name = "JD Sports",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.47f,
            y = 0.13f,
            subtitle = "King of Trainers & Sports Fashion",
            description = "Nike, Adidas, Under Armour sportswear, hoodies, trainers, and Scottish national team kits.",
            operatingHours = "Open 04:30 - 20:30",
            walkingTimeMin = 3,
            walkingDistanceMeters = 110
        ),
        TerminalPlace(
            id = "accessorize",
            name = "Accessorize London",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.50f,
            y = 0.12f,
            subtitle = "Jewellery, Bags & Beachwear",
            description = "Travel accessories, sunglasses, bags, jewellery, and scarves.",
            operatingHours = "Open 04:30 - 20:00",
            walkingTimeMin = 3,
            walkingDistanceMeters = 120
        ),
        TerminalPlace(
            id = "edinburgh-gin",
            name = "Edinburgh Gin Boutique",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.27f,
            y = 0.29f,
            subtitle = "Local Craft Gin Distillers",
            description = "Exclusive airport limited editions, Rhubarb & Ginger gin, tasting samples, and luxury gift sets.",
            operatingHours = "Open 04:30 - 20:30",
            walkingTimeMin = 2,
            walkingDistanceMeters = 90
        ),
        TerminalPlace(
            id = "brora",
            name = "Brora Cashmere",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.30f,
            y = 0.29f,
            subtitle = "Luxury Scottish Knitwear",
            description = "High-end British cashmere fashion, cardigans, and accessories crafted in Scotland.",
            operatingHours = "Open 05:00 - 20:00",
            walkingTimeMin = 2,
            walkingDistanceMeters = 90
        ),
        TerminalPlace(
            id = "rituals",
            name = "Rituals Cosmetics",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.32f,
            y = 0.29f,
            subtitle = "Luxury Bath, Body & Home Fragrances",
            description = "Body creams, shower foams, travel gift sets, and soothing aromas.",
            operatingHours = "Open 05:00 - 20:30",
            walkingTimeMin = 2,
            walkingDistanceMeters = 95
        ),
        TerminalPlace(
            id = "tartan-weaving",
            name = "Tartan Weaving Mill",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.34f,
            y = 0.29f,
            subtitle = "Authentic Scottish Cashmere & Kilts",
            description = "100% pure cashmere scarves, Harris Tweed bags, tartan souvenirs, and Highland gifts.",
            operatingHours = "Open 04:30 - 20:30",
            walkingTimeMin = 2,
            walkingDistanceMeters = 95
        ),
        TerminalPlace(
            id = "haute-parfumerie",
            name = "Haute Parfumerie",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.43f,
            y = 0.29f,
            subtitle = "Bespoke Niche Fragrances",
            description = "Exclusive niche luxury perfume houses including Creed, Tom Ford Private Blend, and Jo Malone.",
            operatingHours = "Open 05:00 - 20:30",
            walkingTimeMin = 2,
            walkingDistanceMeters = 100
        ),
        TerminalPlace(
            id = "fatface",
            name = "FatFace",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.46f,
            y = 0.31f,
            subtitle = "Casual British Lifestyle Clothing",
            description = "High-quality knitwear, tees, jackets, and accessories designed for travel comfort.",
            operatingHours = "Open 04:30 - 20:00",
            walkingTimeMin = 3,
            walkingDistanceMeters = 110
        ),
        TerminalPlace(
            id = "inmotion",
            name = "InMotion Electronics",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.49f,
            y = 0.31f,
            subtitle = "Headphones, Chargers & Travel Tech",
            description = "Apple, Bose, Sony, and JBL noise-cancelling headphones, travel adapters, and high-speed power banks.",
            operatingHours = "Open 04:00 - 21:00",
            walkingTimeMin = 3,
            walkingDistanceMeters = 115
        ),
        TerminalPlace(
            id = "sunglass-hut",
            name = "Sunglass Hut",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.59f,
            y = 0.30f,
            subtitle = "Designer Sunglasses",
            description = "Ray-Ban, Oakley, Prada, Gucci, and Tom Ford duty-free eyewear.",
            operatingHours = "Open 04:30 - 20:30",
            walkingTimeMin = 3,
            walkingDistanceMeters = 130
        ),
        TerminalPlace(
            id = "heritage-scotland",
            name = "Heritage of Scotland",
            category = PlaceCategory.SHOPPING,
            level = TerminalLevel.LEVEL_1,
            x = 0.64f,
            y = 0.33f,
            subtitle = "Scottish Souvenirs, Shortbread & Gifts",
            description = "Highland shortbread, tablet, iron brew fudge, whisky miniatures, and Scottish gifts.",
            operatingHours = "Open 04:30 - 20:30",
            walkingTimeMin = 4,
            walkingDistanceMeters = 160
        ),

        // --- AIRPORT LOUNGES ---
        TerminalPlace(
            id = "lounge-ba",
            name = "British Airways Lounge",
            category = PlaceCategory.LOUNGES,
            level = TerminalLevel.LEVEL_1,
            x = 0.13f,
            y = 0.37f,
            subtitle = "Beside Gate 4 • Club Europe & Executive Club",
            description = "Dedicated BA lounge featuring business workstations, complimentary Scottish gin bar, hot buffet, and runway views.",
            operatingHours = "Open 05:00 - 20:30",
            walkingTimeMin = 4,
            walkingDistanceMeters = 170
        ),
        TerminalPlace(
            id = "lounge-escape",
            name = "Escape Lounge",
            category = PlaceCategory.LOUNGES,
            level = TerminalLevel.LEVEL_1,
            x = 0.10f,
            y = 0.38f,
            subtitle = "Beside Gate 4 • Open to All Passengers",
            description = "Relaxing pre-flight lounge with chef-curated hot buffet, barista coffee, quiet zone, and complimentary bar.",
            operatingHours = "Open 04:30 - 20:30",
            walkingTimeMin = 4,
            walkingDistanceMeters = 180
        ),
        TerminalPlace(
            id = "lounge-aspire",
            name = "The Aspire Lounge (The Kin)",
            category = PlaceCategory.LOUNGES,
            level = TerminalLevel.LEVEL_1,
            x = 0.81f,
            y = 0.46f,
            subtitle = "Beside Gates 13–14 • Priority Pass & DragonPass",
            description = "Contemporary lounge featuring private work booths, runway views, artisan Scottish drinks, and artisan dining.",
            operatingHours = "Open 04:30 - 21:00",
            walkingTimeMin = 5,
            walkingDistanceMeters = 260
        ),

        // --- PASSENGER SERVICES ---
        TerminalPlace(
            id = "service-water-west",
            name = "Water Refill Station (West)",
            category = PlaceCategory.SERVICES,
            level = TerminalLevel.LEVEL_1,
            x = 0.25f,
            y = 0.37f,
            subtitle = "Beside Toilets & Giraffe",
            description = "Free filtered, chilled drinking water bottle refill fountain.",
            operatingHours = "Open 24/7",
            walkingTimeMin = 2,
            walkingDistanceMeters = 90
        ),
        TerminalPlace(
            id = "service-water-east",
            name = "Water Refill Station (East)",
            category = PlaceCategory.SERVICES,
            level = TerminalLevel.LEVEL_1,
            x = 0.74f,
            y = 0.39f,
            subtitle = "Near Gate 12 & Seoul Bird",
            description = "Chilled drinking water bottle fountain.",
            operatingHours = "Open 24/7",
            walkingTimeMin = 4,
            walkingDistanceMeters = 200
        ),
        TerminalPlace(
            id = "service-water-pier",
            name = "Water Refill Station (South Pier)",
            category = PlaceCategory.SERVICES,
            level = TerminalLevel.LEVEL_1,
            x = 0.81f,
            y = 0.78f,
            subtitle = "Gates 17-30 Pier Walkway",
            description = "Chilled drinking water fountain for pier passengers.",
            operatingHours = "Open 24/7",
            walkingTimeMin = 6,
            walkingDistanceMeters = 350
        ),

        // ==========================================================
        // === LEVEL 0: GROUND FLOOR CHECK-IN, ARRIVALS & TRANSIT ===
        // ==========================================================
        TerminalPlace(
            id = "checkin-zone-a",
            name = "Check-in Desks 1-15 (Zone A)",
            category = PlaceCategory.SERVICES,
            level = TerminalLevel.LEVEL_0,
            x = 0.25f,
            y = 0.50f,
            subtitle = "easyJet, Ryanair & Self-Bag Drop",
            description = "Automated self-service bag drop kiosks and airline ticket desks for Zone A carriers.",
            operatingHours = "Open 03:30 - 21:00",
            walkingTimeMin = 2,
            walkingDistanceMeters = 80
        ),
        TerminalPlace(
            id = "checkin-zone-b",
            name = "Check-in Desks 16-30 (Zone B)",
            category = PlaceCategory.SERVICES,
            level = TerminalLevel.LEVEL_0,
            x = 0.55f,
            y = 0.50f,
            subtitle = "British Airways, KLM, Qatar, Lufthansa",
            description = "Full service check-in desks, club / premium desks, and oversized luggage acceptance.",
            operatingHours = "Open 04:00 - 20:30",
            walkingTimeMin = 2,
            walkingDistanceMeters = 90
        ),
        TerminalPlace(
            id = "baggage-claim-domestic",
            name = "Baggage Reclaim (Carousels 1-3)",
            category = PlaceCategory.SERVICES,
            level = TerminalLevel.LEVEL_0,
            x = 0.20f,
            y = 0.32f,
            subtitle = "UK & Ireland Domestic Arrivals",
            description = "Domestic arrivals hall with carousels 1, 2, and 3, plus Swissport / Menzies baggage enquiry desks.",
            operatingHours = "Open 24/7",
            walkingTimeMin = 3,
            walkingDistanceMeters = 150
        ),
        TerminalPlace(
            id = "baggage-claim-intl",
            name = "Baggage Reclaim (Carousels 4-6)",
            category = PlaceCategory.SERVICES,
            level = TerminalLevel.LEVEL_0,
            x = 0.65f,
            y = 0.32f,
            subtitle = "International Arrivals & UK Border",
            description = "International arrivals baggage carousels 4, 5, and 6 following e-Passport gates and customs inspection.",
            operatingHours = "Open 24/7",
            walkingTimeMin = 4,
            walkingDistanceMeters = 190
        ),
        TerminalPlace(
            id = "tram-station",
            name = "Edinburgh Trams Station",
            category = PlaceCategory.TRANSPORT,
            level = TerminalLevel.LEVEL_0,
            x = 0.55f,
            y = 0.78f,
            subtitle = "Every 7 mins to City Centre & Newhaven",
            description = "Direct airport tram terminus. Covered walkway from terminal. Journey time ~30 mins to Princes Street.",
            operatingHours = "Runs 05:30 - 23:30",
            walkingTimeMin = 3,
            walkingDistanceMeters = 160
        ),
        TerminalPlace(
            id = "bus-airlink",
            name = "Airlink 100 Express Bus (Stance D)",
            category = PlaceCategory.TRANSPORT,
            level = TerminalLevel.LEVEL_0,
            x = 0.40f,
            y = 0.72f,
            subtitle = "24/7 Service to Waverley Station",
            description = "Dedicated Lothian blue double-decker bus with free Wi-Fi, luggage racks, and USB charging. Departs every 10 mins.",
            operatingHours = "24 Hours Service",
            walkingTimeMin = 2,
            walkingDistanceMeters = 110
        ),
        TerminalPlace(
            id = "pickup-dropoff",
            name = "Pick-up & Drop-off Area",
            category = PlaceCategory.TRANSPORT,
            level = TerminalLevel.LEVEL_0,
            x = 0.35f,
            y = 0.65f,
            subtitle = "Terminal Forecourt",
            description = "Designated 10-minute passenger drop-off and collection bays opposite terminal entrance.",
            operatingHours = "24 Hours",
            walkingTimeMin = 1,
            walkingDistanceMeters = 50
        )
    )

    // User location default: just after Duty Free walkthrough exit into central concourse
    val defaultUserLocation = Pair(0.38f, 0.26f)

    /**
     * Calculates authentic turn-by-turn indoor corridor waypoints
     * adhering strictly to official terminal pedestrian hallways (NO straight lines through walls/shops!)
     */
    fun getRouteTo(destination: TerminalPlace, startPos: Pair<Float, Float> = defaultUserLocation): WayfindingRoute {
        val waypoints = mutableListOf<Pair<Float, Float>>()
        val steps = mutableListOf<NavigationStep>()

        waypoints.add(startPos)

        // 1. If starting inside Security / Lower Duty Free area
        var curX = startPos.first
        var curY = startPos.second

        if (curY > 0.40f) {
            // Traverse the Duty Free snake walkthrough
            waypoints.add(Pair(0.36f, 0.48f))
            waypoints.add(Pair(0.42f, 0.42f))
            waypoints.add(Pair(0.40f, 0.32f))
            waypoints.add(Pair(0.38f, 0.25f)) // Central spine junction
            steps.add(NavigationStep("Pass through World Duty Free walkthrough", 70, StepIcon.STRAIGHT))
            curX = 0.38f
            curY = 0.25f
        }

        val spineY = 0.25f // Main horizontal concourse hallway between North shops and Central island

        // Ensure we connect cleanly to the Central Concourse Spine
        if (kotlin.math.abs(curY - spineY) > 0.02f && curY < 0.40f && destination.y < 0.40f) {
            waypoints.add(Pair(curX, spineY))
            curY = spineY
        }

        // Branching based on destination sector:
        if (destination.x < 0.22f) {
            // === WEST WING ROUTING ===
            waypoints.add(Pair(0.20f, spineY))
            steps.add(NavigationStep("Head west along Central Concourse past Caffè Nero", 60, StepIcon.TURN_LEFT))

            if (destination.y > 0.45f) {
                // Towards Gates 1-3 link corridor
                waypoints.add(Pair(0.20f, 0.40f))
                waypoints.add(Pair(0.14f, 0.48f))
                waypoints.add(Pair(0.06f, 0.53f))
                waypoints.add(Pair(destination.x, destination.y))
                steps.add(NavigationStep("Turn left past Giraffe into the Gates 1-3 walkway", 80, StepIcon.TURN_LEFT))
                steps.add(NavigationStep("Arrive at ${destination.name}", 40, StepIcon.DESTINATION))
            } else if (destination.y < 0.15f) {
                // Towards All Bar One West & Gate 6
                waypoints.add(Pair(0.20f, 0.18f))
                waypoints.add(Pair(0.14f, 0.18f))
                waypoints.add(Pair(destination.x, destination.y))
                steps.add(NavigationStep("Turn right towards Gate 6 and All Bar One", 50, StepIcon.TURN_RIGHT))
                steps.add(NavigationStep("Arrive at ${destination.name}", 20, StepIcon.DESTINATION))
            } else {
                // Towards Giraffe, BA Lounge, Escape Lounge, Gates 4-5
                waypoints.add(Pair(0.20f, destination.y)) // Walk straight down hallway
                waypoints.add(Pair(destination.x, destination.y)) // Turn directly into entrance
                steps.add(NavigationStep("Turn into West Wing concourse", 40, StepIcon.TURN_LEFT))
                steps.add(NavigationStep("Arrive at ${destination.name}", destination.walkingDistanceMeters, StepIcon.DESTINATION))
            }
        } else if (destination.x > 0.70f) {
            // === EAST WING & SOUTH-EAST PIER ROUTING ===
            waypoints.add(Pair(0.55f, spineY))
            waypoints.add(Pair(0.72f, spineY))
            steps.add(NavigationStep("Head east along Central Concourse past Boots and Starbucks", 80, StepIcon.STRAIGHT))

            waypoints.add(Pair(0.74f, 0.34f)) // Beside Burger King / Gate 12
            steps.add(NavigationStep("Continue past Burger King into East Concourse", 40, StepIcon.STRAIGHT))

            if (destination.y > 0.68f) {
                // South-East Pier (Gates 17-30)
                waypoints.add(Pair(0.81f, 0.50f))
                waypoints.add(Pair(0.81f, 0.68f))
                steps.add(NavigationStep("Continue past Icons Sports Bar towards Gates 17-30", 90, StepIcon.STRAIGHT))

                waypoints.add(Pair(0.82f, 0.74f))
                steps.add(NavigationStep("Enter South-East Pier walkway", 50, StepIcon.STRAIGHT))

                // Walk down the pier corridor directly to the gate coordinate
                waypoints.add(Pair(0.82f, destination.y))
                waypoints.add(Pair(destination.x, destination.y))
                steps.add(NavigationStep("Continue along pier corridor to ${destination.name}", destination.walkingDistanceMeters, StepIcon.DESTINATION))
            } else if (destination.y < 0.30f) {
                // Towards Forth & Main
                waypoints.add(Pair(0.68f, 0.20f))
                waypoints.add(Pair(destination.x, destination.y))
                steps.add(NavigationStep("Turn into Forth & Main entrance", 30, StepIcon.DESTINATION))
            } else {
                // East Concourse: Gates 13-16, Pret East, Aspire Lounge, Icons Sports Bar, Seoul Bird
                waypoints.add(Pair(0.81f, destination.y))
                waypoints.add(Pair(destination.x, destination.y))
                steps.add(NavigationStep("Head into East Pier towards ${destination.name}", 50, StepIcon.DESTINATION))
            }
        } else {
            // === CENTRAL CONCOURSE ROUTING ===
            if (destination.y < 0.18f) {
                // North retail row: Boots, WHSmith, JD, Accessorize, Gates 7-11
                waypoints.add(Pair(destination.x, spineY))
                waypoints.add(Pair(destination.x, destination.y))
                steps.add(NavigationStep("Walk along Central Concourse to ${destination.name}", destination.walkingDistanceMeters, StepIcon.DESTINATION))
            } else if (destination.y > 0.28f) {
                // Central Island: Edinburgh Gin, Brora, Tartan Weaving, FatFace, InMotion, Sunglass Hut
                waypoints.add(Pair(destination.x, spineY))
                waypoints.add(Pair(destination.x, destination.y))
                steps.add(NavigationStep("Head into Central Retail Island to ${destination.name}", destination.walkingDistanceMeters, StepIcon.DESTINATION))
            } else {
                // On the spine (Fever-Tree, Starbucks, Wetherspoon)
                waypoints.add(Pair(destination.x, destination.y))
                steps.add(NavigationStep("Walk directly along concourse to ${destination.name}", destination.walkingDistanceMeters, StepIcon.DESTINATION))
            }
        }

        // Clean redundant duplicate adjacent points
        val cleanWaypoints = mutableListOf<Pair<Float, Float>>()
        for (pt in waypoints) {
            if (cleanWaypoints.isEmpty() ||
                kotlin.math.hypot(pt.first - cleanWaypoints.last().first, pt.second - cleanWaypoints.last().second) > 0.015f) {
                cleanWaypoints.add(pt)
            }
        }
        if (cleanWaypoints.size < 2 || cleanWaypoints.last() != Pair(destination.x, destination.y)) {
            cleanWaypoints.add(Pair(destination.x, destination.y))
        }

        return WayfindingRoute(
            fromName = if (startPos.second > 0.45f) "Central Security Checkpoint" else "Central Concourse (Duty Free Exit)",
            toPlace = destination,
            estimatedMinutes = destination.walkingTimeMin,
            distanceMeters = destination.walkingDistanceMeters,
            waypoints = cleanWaypoints,
            steps = steps
        )
    }
}
