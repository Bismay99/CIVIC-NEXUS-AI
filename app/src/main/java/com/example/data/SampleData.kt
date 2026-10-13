package com.example.data

import com.example.model.*

object SampleData {

    val initialCityPulse = CityPulseStats(
        pulseScore = 92,
        activeIncidents = 14,
        resolvedToday = 38,
        avgResponseMinutes = 24,
        sensorHealthPercent = 98,
        selfHealingActionsCount = 86
    )

    val citizenProfile = CitizenProfile(
        uid = "CN-CITIZEN-001",
        name = "Subhransu Mohapatra",
        citizenId = "CN-OD-2026-7841",
        ward = "Ward 14 (Tech Hub / Patia)",
        trustScore = 98,
        reportsCount = 12,
        resolvedCount = 10,
        impactPoints = 840,
        email = "citizen.reporter@civicnexus.gov.in",
        phone = "+91 98765 43210",
        role = "citizen"
    )

    val emergencyAlerts = listOf(
        EmergencyAlert(
            id = "ALT-2026-01",
            title = "Urban Drainage Surcharge Advisory",
            severity = IncidentSeverity.HIGH,
            area = "Sector 4 — Rasulgarh Flyover Undercrossing",
            details = "Smart telemetry detected water accumulation reaching 18cm. Automated auxiliary pumps activated. Traffic rerouting in progress.",
            timestamp = "12 mins ago",
            issuingAuthority = "Bhubaneswar Smart City Integrated Command Center",
            isSimulated = true
        ),
        EmergencyAlert(
            id = "ALT-2026-02",
            title = "High-Voltage Cable Insulation Alert",
            severity = IncidentSeverity.CRITICAL,
            area = "Sector 2 — Master Canteen Plaza",
            details = "Underground sensor tripped alert. TPCODL technical team dispatched for immediate isolation. Pedestrian access restricted.",
            timestamp = "35 mins ago",
            issuingAuthority = "Power Distribution Authority",
            isSimulated = true
        ),
        EmergencyAlert(
            id = "ALT-2026-03",
            title = "Smart Traffic Green-Wave Active",
            severity = IncidentSeverity.MEDIUM,
            area = "Janpath Arterial Corridor",
            details = "Automated priority signal routing for Emergency 108 Ambulance en route to Capital Hospital.",
            timestamp = "1 hr ago",
            issuingAuthority = "Traffic Automated Management System",
            isSimulated = true
        )
    )

    val initialIncidents = listOf(
        Incident(
            id = "INC-801",
            title = "Severe Pothole Cluster on Arterial Lane",
            category = IncidentCategory.POTHOLES,
            severity = IncidentSeverity.HIGH,
            status = "Assigned",
            locationName = "Infocity Square to Patia Road",
            latitude = 20.3533,
            longitude = 85.8172,
            description = "Multiple depressions following heavy rain. Asphalt sub-base destabilized. Causes lane slowdown.",
            reportedTime = "25 mins ago",
            department = "Works Department (Road Division 2)",
            verifiedCount = 6,
            isSimulated = true
        ),
        Incident(
            id = "INC-802",
            title = "Overflowing Municipal Dumpster & Leachate",
            category = IncidentCategory.GARBAGE,
            severity = IncidentSeverity.MEDIUM,
            status = "In Progress",
            locationName = "Saheed Nagar Commercial Complex",
            latitude = 20.2882,
            longitude = 85.8456,
            description = "Biowaste overflow near grocery stalls. Smart bin sensor triggered capacity alert 92%.",
            reportedTime = "45 mins ago",
            department = "Sanitation & Waste Management Authority",
            verifiedCount = 4,
            isSimulated = true
        ),
        Incident(
            id = "INC-803",
            title = "Stormwater Drain Clog & Waterlogging",
            category = IncidentCategory.FLOODING,
            severity = IncidentSeverity.CRITICAL,
            status = "In Progress",
            locationName = "Nayapalli Behera Sahi Lowland",
            latitude = 20.3015,
            longitude = 85.8205,
            description = "Storm drain choked with silt debris. Water level rising toward sidewalk curb.",
            reportedTime = "15 mins ago",
            department = "Drainage & Stormwater Division",
            verifiedCount = 11,
            isSimulated = true
        ),
        Incident(
            id = "INC-804",
            title = "Extinguished Streetlights on Link Road",
            category = IncidentCategory.STREETLIGHTS,
            severity = IncidentSeverity.LOW,
            status = "Reported",
            locationName = "Khandagiri Heritage Ring Road",
            latitude = 20.2589,
            longitude = 85.7820,
            description = "Four consecutive LED luminaire fixtures inactive. Decreased evening visibility.",
            reportedTime = "2 hrs ago",
            department = "Public Electrical Utility",
            verifiedCount = 3,
            isSimulated = true
        ),
        Incident(
            id = "INC-805",
            title = "Pressurized Main Line Fracture",
            category = IncidentCategory.WATER_BURST,
            severity = IncidentSeverity.HIGH,
            status = "In Progress",
            locationName = "Chandrasekharpur Sector B",
            latitude = 20.3220,
            longitude = 85.8210,
            description = "Potable water supply line rupture with surface bubbling. Flow loss estimated at 120 LPM.",
            reportedTime = "50 mins ago",
            department = "WATCO Water Services",
            verifiedCount = 8,
            isSimulated = true
        ),
        Incident(
            id = "INC-806",
            title = "Gridlock at University Intersection",
            category = IncidentCategory.TRAFFIC,
            severity = IncidentSeverity.MEDIUM,
            status = "Dispatched",
            locationName = "KIIT Knowledge Campus Crossway",
            latitude = 20.3540,
            longitude = 85.8190,
            description = "Traffic signal timing desync with vehicle queue extending 350 meters.",
            reportedTime = "10 mins ago",
            department = "Smart Traffic Command",
            verifiedCount = 15,
            isSimulated = true
        )
    )

    val emergencyFacilities = listOf(
        EmergencyFacility(
            id = "FAC-01",
            name = "AIIMS Bhubaneswar Trauma Center",
            type = "Super-Specialty Hospital",
            contactNumber = "+91 674 2476789",
            address = "Sijua, Patrapada, Bhubaneswar",
            distanceKm = 4.2,
            latitude = 20.2312,
            longitude = 85.7725,
            isOperational = true
        ),
        EmergencyFacility(
            id = "FAC-02",
            name = "Capital Hospital Central Emergency",
            type = "Public District Hospital",
            contactNumber = "+91 674 2391983",
            address = "Unit 6, Old Town, Bhubaneswar",
            distanceKm = 2.8,
            latitude = 20.2642,
            longitude = 85.8285,
            isOperational = true
        ),
        EmergencyFacility(
            id = "FAC-03",
            name = "Odisha Fire & Disaster Control Station",
            type = "Fire & Disaster Response",
            contactNumber = "101",
            address = "Kalpana Square Central Post",
            distanceKm = 3.5,
            latitude = 20.2605,
            longitude = 85.8410,
            isOperational = true
        ),
        EmergencyFacility(
            id = "FAC-04",
            name = "ODRAF 1st Battalion Rapid Base",
            type = "Disaster Rescue Force",
            contactNumber = "+91 674 2530112",
            address = "Chandaka Disaster Hub",
            distanceKm = 6.1,
            latitude = 20.3601,
            longitude = 85.7890,
            isOperational = true
        ),
        EmergencyFacility(
            id = "FAC-05",
            name = "Urban Police Control Room (Dial 112)",
            type = "Police Headquarters",
            contactNumber = "112",
            address = "Vani Vihar Transit Circle",
            distanceKm = 1.9,
            latitude = 20.2970,
            longitude = 85.8360,
            isOperational = true
        )
    )

    val initialComplaints = listOf(
        Complaint(
            referenceId = "CN-2026-1042",
            category = IncidentCategory.POTHOLES,
            title = "Deep crater on KIIT main avenue",
            description = "Substantial asphalt breakdown after monsoonal water surge. 1.2m diameter hole endangering two-wheelers.",
            hasPhoto = true,
            photoName = "pothole_patia_lane.jpg",
            latitude = 20.3521,
            longitude = 85.8184,
            address = "Plot 42, Avenue 3, KIIT Innovation Enclave, Patia",
            submittedDate = "Today, 09:15 AM",
            status = ComplaintStatus.IN_PROGRESS,
            assignedOfficer = "Er. Ramesh Mohanty (Senior Works Engineer)",
            timeline = listOf(
                TimelineStep(ComplaintStatus.SUBMITTED, "Citizen Report Logged", "09:15 AM", "Citizen Subhransu logged report with GPS accuracy 4.2m", true, false),
                TimelineStep(ComplaintStatus.UNDER_REVIEW, "AI Incident Analysis Verified", "09:16 AM", "Severity classified High; assigned to Municipal Road Div", true, false),
                TimelineStep(ComplaintStatus.ASSIGNED, "Work Crew Assigned", "09:42 AM", "Assigned to Er. Ramesh Mohanty; Rapid Patch Unit 03", true, false),
                TimelineStep(ComplaintStatus.IN_PROGRESS, "Maintenance Vehicle On-Site", "10:30 AM", "Cold-mix asphalt application in progress; lane flagged", true, true),
                TimelineStep(ComplaintStatus.RESOLVED, "Civic Remediation Verified", "Pending", "Awaiting sensor QA & post-fix photo upload", false, false)
            ),
            aiAnalysis = AiAnalysisResult(
                suggestedCategory = IncidentCategory.POTHOLES,
                incidentSummary = "Significant road pavement hazard with high structural risk for motorcycles.",
                suggestedPriority = IncidentSeverity.HIGH,
                recommendedDepartment = "Municipal Corporation Road Infrastructure Division",
                reasoning = "Visual detection shows exposed base aggregate with sharp pavement edges over 12cm depth in high-transit corridor.",
                confidenceScore = 96,
                suggestedPrecaution = "Place reflective hazard cone and avoid immediate curbside lane."
            )
        ),
        Complaint(
            referenceId = "CN-2026-0985",
            category = IncidentCategory.GARBAGE,
            title = "Secondary Waste Collection Bin Spillage",
            description = "Commercial waste collection bin overflowing for 36 hours. Stray animal nuisance.",
            hasPhoto = true,
            photoName = "garbage_saheed_nagar.jpg",
            latitude = 20.2890,
            longitude = 85.8460,
            address = "Market Building Lane 4, Saheed Nagar",
            submittedDate = "Yesterday, 04:20 PM",
            status = ComplaintStatus.RESOLVED,
            assignedOfficer = "Officer Sunita Jena (Zonal Sanitation Inspector)",
            timeline = listOf(
                TimelineStep(ComplaintStatus.SUBMITTED, "Citizen Report Logged", "Yesterday, 04:20 PM", "Citizen report captured with photographic evidence", true, false),
                TimelineStep(ComplaintStatus.UNDER_REVIEW, "Automated Triage Completed", "Yesterday, 04:22 PM", "Waste collection route auto-optimized", true, false),
                TimelineStep(ComplaintStatus.ASSIGNED, "Sanitation Crew Dispatched", "Yesterday, 04:45 PM", "Assigned to Sanitation Inspector Sunita Jena", true, false),
                TimelineStep(ComplaintStatus.IN_PROGRESS, "Compactor Truck Clean-up", "Yesterday, 06:10 PM", "Waste cleared and bin sanitized with disinfectant spray", true, false),
                TimelineStep(ComplaintStatus.RESOLVED, "Remediation Complete", "Yesterday, 07:05 PM", "Resolution confirmed via telemetry & photo upload", true, false)
            ),
            aiAnalysis = AiAnalysisResult(
                suggestedCategory = IncidentCategory.GARBAGE,
                incidentSummary = "Municipal solid waste overflow with sanitation and vector risk.",
                suggestedPriority = IncidentSeverity.MEDIUM,
                recommendedDepartment = "Sanitation & Solid Waste Management",
                reasoning = "Bin fill volume exceeds 100% capacity; high organic content near food establishments.",
                confidenceScore = 94,
                suggestedPrecaution = "Maintain 5m distance from decomposing debris."
            )
        ),
        Complaint(
            referenceId = "CN-2026-0914",
            category = IncidentCategory.STREETLIGHTS,
            title = "Intermittent LED lighting failure",
            description = "Dark stretch spanning 200m on pedestrian walkway near stadium.",
            hasPhoto = false,
            photoName = "",
            latitude = 20.2910,
            longitude = 85.8320,
            address = "Kalinga Stadium Outer Perimeter Path",
            submittedDate = "2 days ago",
            status = ComplaintStatus.RESOLVED,
            assignedOfficer = "P. K. Mishra (Public Lighting Dept)",
            timeline = listOf(
                TimelineStep(ComplaintStatus.SUBMITTED, "Citizen Report Logged", "2 days ago", "Citizen report submitted", true, false),
                TimelineStep(ComplaintStatus.UNDER_REVIEW, "Feeder Pillar Checked", "2 days ago", "Automated telemetry verified low circuit current", true, false),
                TimelineStep(ComplaintStatus.ASSIGNED, "Electrician Dispatched", "2 days ago", "Dispatched maintenance van", true, false),
                TimelineStep(ComplaintStatus.IN_PROGRESS, "Driver Module Replaced", "Yesterday, 11:00 AM", "Replacement of burnt LED power driver", true, false),
                TimelineStep(ComplaintStatus.RESOLVED, "All 4 Lights Functional", "Yesterday, 01:15 PM", "Lighting lux test validated", true, false)
            ),
            aiAnalysis = AiAnalysisResult(
                suggestedCategory = IncidentCategory.STREETLIGHTS,
                incidentSummary = "Public lighting blackout along pedestrian pathway.",
                suggestedPriority = IncidentSeverity.LOW,
                recommendedDepartment = "Public Electrical Utility",
                reasoning = "Sensor diagnostic confirmed voltage fluctuation on circuit branch 7.",
                confidenceScore = 91,
                suggestedPrecaution = "Use phone torch or lighted main avenue until repairs finish."
            )
        )
    )

    val initialNotifications = listOf(
        AppNotification(
            id = "NOTIF-01",
            title = "Complaint Update: In Progress",
            message = "Your report CN-2026-1042 has maintenance crew on-site with Rapid Patch Unit 03.",
            category = "Complaint Update",
            timestamp = "10 mins ago",
            isRead = false,
            isSimulated = true
        ),
        AppNotification(
            id = "NOTIF-02",
            title = "Smart City Flash Flood Warning",
            message = "Automated advisory for Rasulgarh undercrossing. Auxiliary storm pumps active.",
            category = "Emergency Broadcast",
            timestamp = "30 mins ago",
            isRead = false,
            isSimulated = true
        ),
        AppNotification(
            id = "NOTIF-03",
            title = "Complaint Resolved: CN-2026-0985",
            message = "Waste overflow at Saheed Nagar Commercial Complex has been cleared and sanitized.",
            category = "Complaint Update",
            timestamp = "Yesterday",
            isRead = true,
            isSimulated = true
        ),
        AppNotification(
            id = "NOTIF-04",
            title = "Civic Nexus Core Showcase",
            message = "Self-Healing City nervous system active in Sector Sandbox mode. Multi-agency telemetry live.",
            category = "Department Notice",
            timestamp = "Today",
            isRead = true,
            isSimulated = true
        )
    )

    val initialChatMessages = listOf(
        ChatMessage(
            id = "MSG-01",
            text = "Welcome to CIVIC NEXUS AI — The AI Nervous System for a Self-Healing City.\n\nI am your municipal intelligence assistant. How can I assist you with civic services, incident reporting, emergency coordination, or tracking existing complaints today?",
            isUser = false,
            timestamp = "Just now",
            suggestedActions = listOf(
                "How do I report a pothole?",
                "Status of complaint CN-2026-1042",
                "Emergency helpline contacts",
                "How does self-healing city AI work?"
            )
        )
    )

    val defaultEmergencySimulation = EmergencySimulation(
        incidentTitle = "Flash Waterlogging & Electrical Conduit Breach",
        location = "Sector 3 — Rasulgarh Underpass & Substation Corridor",
        severity = IncidentSeverity.CRITICAL,
        steps = listOf(
            CoordinationStep(
                agency = CoordinationAgency.ODRAF_DISASTER,
                action = "Deploy 2 high-capacity submersible dewatering pumps and barrier pontoons",
                status = "Dispatched",
                progress = 0.8f,
                timestamp = "T+2m"
            ),
            CoordinationStep(
                agency = CoordinationAgency.TRAFFIC_AUTHORITY,
                action = "Automate dynamic VMS signage diversion to Alternate Bypass via Vani Vihar",
                status = "Active",
                progress = 1.0f,
                timestamp = "T+3m"
            ),
            CoordinationStep(
                agency = CoordinationAgency.MUNICIPAL_WORKS,
                action = "Remotely isolate 11kV feeder line and engage backup auxiliary breaker",
                status = "Active",
                progress = 0.9f,
                timestamp = "T+4m"
            ),
            CoordinationStep(
                agency = CoordinationAgency.AMBULANCE_CORPS,
                action = "Position 108 Emergency Ambulance at Northern evacuation ingress",
                status = "On Scene",
                progress = 1.0f,
                timestamp = "T+6m"
            ),
            CoordinationStep(
                agency = CoordinationAgency.FIRE_RESCUE,
                action = "Conduct infrared scanning of sub-surface drainage chambers",
                status = "Queued",
                progress = 0.3f,
                timestamp = "T+7m"
            )
        )
    )
}
