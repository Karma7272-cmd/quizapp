package com.example.data

data class Question(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

object DefaultQuizzes {
    val categories = listOf("Science", "History", "Pop Culture", "Gaming")

    fun getQuestions(category: String, difficulty: String): List<Question> {
        val normalizedCategory = category.lowercase().trim()
        val normalizedDiff = difficulty.lowercase().trim()

        return when (normalizedCategory) {
            "science" -> when (normalizedDiff) {
                "easy" -> listOf(
                    Question(
                        1,
                        "What is the chemical symbol for Helium?",
                        listOf("H", "He", "Li", "Be"),
                        "He",
                        "The chemical symbol for Helium is He. Its atomic number is 2."
                    ),
                    Question(
                        2,
                        "Which planet is closest to the Sun?",
                        listOf("Venus", "Earth", "Mercury", "Mars"),
                        "Mercury",
                        "Mercury is the closest planet to the Sun in our Solar System."
                    ),
                    Question(
                        3,
                        "What is the boiling point of pure water in Celsius?",
                        listOf("50°C", "100°C", "150°C", "200°C"),
                        "100°C",
                        "Pure water boils at 100°C under standard atmospheric conditions."
                    ),
                    Question(
                        4,
                        "Which of these is the main pigment used in photosynthesis?",
                        listOf("Chlorophyll", "Carotenoid", "Phycobilin", "Melanin"),
                        "Chlorophyll",
                        "Chlorophyll is the green pigment in plants that absorbs light energy."
                    ),
                    Question(
                        5,
                        "What is the center of an atom called?",
                        listOf("Proton", "Electron", "Nucleus", "Neutron"),
                        "Nucleus",
                        "The nucleus is the small, dense region at the center of an atom."
                    )
                )
                "medium" -> listOf(
                    Question(
                        1,
                        "Which metal is the best conductor of electricity?",
                        listOf("Copper", "Silver", "Gold", "Aluminum"),
                        "Silver",
                        "Silver is the best conductor of electricity, followed closely by copper."
                    ),
                    Question(
                        2,
                        "What is the powerhouse of the cell?",
                        listOf("Nucleus", "Ribosome", "Mitochondria", "Lysosome"),
                        "Mitochondria",
                        "Mitochondria convert nutrients into energy (ATP), earning them this moniker."
                    ),
                    Question(
                        3,
                        "How many elements are in the periodic table?",
                        listOf("105", "112", "118", "124"),
                        "118",
                        "The periodic table has 118 confirmed chemical elements."
                    ),
                    Question(
                        4,
                        "What is the speed of light in a vacuum?",
                        listOf("approx 150,000 km/s", "approx 300,000 km/s", "approx 450,000 km/s", "approx 600,000 km/s"),
                        "approx 300,000 km/s",
                        "The speed of light is approximately 299,792 kilometers per second."
                    ),
                    Question(
                        5,
                        "Who formulated the laws of planetary motion?",
                        listOf("Isaac Newton", "Galileo Galilei", "Johannes Kepler", "Nicolaus Copernicus"),
                        "Johannes Kepler",
                        "Kepler published his three laws of planetary motion in the early 17th century."
                    )
                )
                else -> listOf( // hard
                    Question(
                        1,
                        "What is the most abundant transition metal in the human body?",
                        listOf("Zinc", "Iron", "Copper", "Manganese"),
                        "Iron",
                        "Iron is the most abundant transition metal, primarily found in blood hemoglobin."
                    ),
                    Question(
                        2,
                        "What quantum mechanical principle states that two identical fermions cannot occupy the same quantum state?",
                        listOf("Heisenberg Uncertainty Principle", "Pauli Exclusion Principle", "Hund's Rule", "Schrödinger Equation"),
                        "Pauli Exclusion Principle",
                        "The Pauli Exclusion Principle dictates the electronic structure of atoms."
                    ),
                    Question(
                        3,
                        "What is the name of the nearest galaxy to the Milky Way?",
                        listOf("Triangulum", "Andromeda", "Large Magellanic Cloud", "Sagittarius"),
                        "Andromeda",
                        "The Andromeda Galaxy is about 2.5 million light-years away and is our closest large galactic neighbor."
                    ),
                    Question(
                        4,
                        "Which particle holds quarks together inside protons and neutrons?",
                        listOf("Gluon", "Boson", "Photon", "Graviton"),
                        "Gluon",
                        "Gluons are gauge bosons that act as the exchange particles for the strong force between quarks."
                    ),
                    Question(
                        5,
                        "What type of chemical bond is formed by the sharing of electron pairs?",
                        listOf("Ionic bond", "Covalent bond", "Hydrogen bond", "Metallic bond"),
                        "Covalent bond",
                        "Covalent bonds involve the stable sharing of electrons between atoms."
                    )
                )
            }
            "history" -> when (normalizedDiff) {
                "easy" -> listOf(
                    Question(
                        1,
                        "Who was the first President of the United States?",
                        listOf("Thomas Jefferson", "Abraham Lincoln", "George Washington", "John Adams"),
                        "George Washington",
                        "George Washington served as president from 1789 to 1797."
                    ),
                    Question(
                        2,
                        "Which ancient civilization built the Coliseum in Rome?",
                        listOf("Greeks", "Egyptians", "Romans", "Persians"),
                        "Romans",
                        "The Roman Empire constructed the Coliseum as a grand amphitheater."
                    ),
                    Question(
                        3,
                        "In which year did World War II end?",
                        listOf("1918", "1941", "1945", "1953"),
                        "1945",
                        "World War II ended in September 1945 with the formal surrender of Japan."
                    ),
                    Question(
                        4,
                        "Who was the famous Queen of Ancient Egypt known for her beauty and alliance with Julis Caesar?",
                        listOf("Nefertiti", "Cleopatra", "Hatshepsut", "Sobekneferu"),
                        "Cleopatra",
                        "Cleopatra VII Philopator was the last active ruler of the Ptolemaic Kingdom of Egypt."
                    ),
                    Question(
                        5,
                        "Which country gifted the Statue of Liberty to the United States?",
                        listOf("United Kingdom", "France", "Spain", "Germany"),
                        "France",
                        "France gifted the Statue of Liberty in 1886 to celebrate the alliance during the American Revolution."
                    )
                )
                "medium" -> listOf(
                    Question(
                        1,
                        "In which century did the Black Death ravage Europe?",
                        listOf("12th Century", "13th Century", "14th Century", "15th Century"),
                        "14th Century",
                        "The bubonic plague swept across Europe mainly between 1346 and 1353."
                    ),
                    Question(
                        2,
                        "Who was the famous female aviator who vanished over the Pacific in 1937?",
                        listOf("Harriet Quimby", "Bessie Coleman", "Amelia Earhart", "Amy Johnson"),
                        "Amelia Earhart",
                        "Amelia Earhart disappeared in 1937 while attempting to fly around the globe."
                    ),
                    Question(
                        3,
                        "What was the name of the series of trade routes connecting China to the Mediterranean?",
                        listOf("Spice Route", "Incense Route", "Silk Road", "Amber Road"),
                        "Silk Road",
                        "The Silk Road network was established during the Han Dynasty."
                    ),
                    Question(
                        4,
                        "Which treaty officially ended World War I?",
                        listOf("Treaty of Paris", "Treaty of Versailles", "Treaty of Ghent", "Treaty of Utrecht"),
                        "Treaty of Versailles",
                        "The Treaty of Versailles was signed in June 1919 in the Palace of Versailles."
                    ),
                    Question(
                        5,
                        "Who was the founder of the Mongol Empire?",
                        listOf("Kublai Khan", "Genghis Khan", "Ogedei Khan", "Tamerlane"),
                        "Genghis Khan",
                        "Genghis Khan united the nomadic tribes of Northeast Asia to form the largest contiguous empire in history."
                    )
                )
                else -> listOf( // hard
                    Question(
                        1,
                        "Who was the prime minister of Britain during most of World War II?",
                        listOf("Neville Chamberlain", "Winston Churchill", "Clement Attlee", "Anthony Eden"),
                        "Winston Churchill",
                        "Winston Churchill led the nation from 1940 to 1945."
                    ),
                    Question(
                        2,
                        "Which Roman Emperor made his horse a senator according to legend?",
                        listOf("Nero", "Caligula", "Commodus", "Claudius"),
                        "Caligula",
                        "Caligula is famously depicted as unstable, culminating in the Senatorial horse legend."
                    ),
                    Question(
                        3,
                        "Which empire was ruled by the Sulieman the Magnificent?",
                        listOf("Mughal Empire", "Safavid Empire", "Ottoman Empire", "Byzantine Empire"),
                        "Ottoman Empire",
                        "Suleiman I commanded the Ottoman Empire at the peak of its economic, military, and political power."
                    ),
                    Question(
                        4,
                        "The Code of Hammurabi, one of the earliest written legal codes, comes from which culture?",
                        listOf("Babylonian", "Assyrian", "Sumerian", "Phoenician"),
                        "Babylonian",
                        "King Hammurabi enacted this code in ancient Mesopotamia (modern-day Iraq)."
                    ),
                    Question(
                        5,
                        "What was the name of the system of state-controlled farm collectives in the early Soviet Union?",
                        listOf("Kolkhoz", "Gulag", "Komsomol", "Gosplan"),
                        "Kolkhoz",
                        "Kolkhozy were collective farms operated alongside state-owned Sovkhozy."
                    )
                )
            }
            "pop culture" -> when (normalizedDiff) {
                "easy" -> listOf(
                    Question(
                        1,
                        "Which wizard boy has a lightning scar on his forehead?",
                        listOf("Percy Jackson", "Harry Potter", "Ron Weasley", "Newt Scamander"),
                        "Harry Potter",
                        "Harry Potter received the curse-induced lightning bolt scar from Lord Voldemort."
                    ),
                    Question(
                        2,
                        "Who is the self-proclaimed 'King of Pop'?",
                        listOf("Elvis Presley", "Michael Jackson", "Prince", "Freddie Mercury"),
                        "Michael Jackson",
                        "Michael Jackson is globally acknowledged as the King of Pop."
                    ),
                    Question(
                        3,
                        "What is the name of the fictional kingdom in Disney's Frozen?",
                        listOf("Arendelle", "Corona", "Genovia", "DunBroch"),
                        "Arendelle",
                        "Elsa and Anna reside in the Norse-inspired kingdom of Arendelle."
                    ),
                    Question(
                        4,
                        "In 'The Simpsons', what is the name of Homer's favorite beer?",
                        listOf("Duff", "Buzz", "Heisler", "Pabst"),
                        "Duff",
                        "Homer Simpson is famously obsessed with Duff Beer."
                    ),
                    Question(
                        5,
                        "Which superhero is also known as the 'Caped Crusader'?",
                        listOf("Superman", "Spider-Man", "Batman", "Iron Man"),
                        "Batman",
                        "Batman is often referred to as the Caped Crusader or the Dark Knight."
                    )
                )
                "medium" -> listOf(
                    Question(
                        1,
                        "How many seasons of the TV show 'Friends' were produced?",
                        listOf("8", "9", "10", "12"),
                        "10",
                        "Friends aired for 10 legendary seasons between 1994 and 2004."
                    ),
                    Question(
                        2,
                        "Which actor played Jack Dawson in the 1997 movie 'Titanic'?",
                        listOf("Brad Pitt", "Johnny Depp", "Leonardo DiCaprio", "Matt Damon"),
                        "Leonardo DiCaprio",
                        "Leonardo DiCaprio starred alongside Kate Winslet in James Cameron's record-breaking film."
                    ),
                    Question(
                        3,
                        "Which movie won the first-ever Academy Award for Best Animated Feature?",
                        listOf("Toy Story", "Shrek", "Monsters, Inc.", "Spirited Away"),
                        "Shrek",
                        "Shrek took home the inaugural Best Animated Feature Oscar in 2002."
                    ),
                    Question(
                        4,
                        "What is the name of the fictional continent in 'Game of Thrones'?",
                        listOf("Westeros", "Essos", "Tamriel", "Middle-earth"),
                        "Westeros",
                        "Most of the narrative of Game of Thrones takes place on the continent of Westeros."
                    ),
                    Question(
                        5,
                        "Which musical artist sang the hit song 'Levitating' in 2020?",
                        listOf("Dua Lipa", "Billie Eilish", "Taylor Swift", "Ariana Grande"),
                        "Dua Lipa",
                        "Dua Lipa released Levitating as part of her critically acclaimed Future Nostalgia album."
                    )
                )
                else -> listOf( // hard
                    Question(
                        1,
                        "Which movie did NOT win 11 Academy Awards?",
                        listOf("Ben-Hur", "Titanic", "Lord of the Rings: Return of the King", "Schindler's List"),
                        "Schindler's List",
                        "Schindler's List won 7 Academy Awards, while the others won a historical 11."
                    ),
                    Question(
                        2,
                        "In Christopher Nolan's 'Inception', what is Cobb's totem to verify reality?",
                        listOf("A chess piece", "A spinning top", "A loaded die", "An antique coin"),
                        "A spinning top",
                        "Cobb uses a small brass spinning top originally belonging to his wife, Mal."
                    ),
                    Question(
                        3,
                        "Who directed the 1994 pulp film 'Pulp Fiction'?",
                        listOf("Martin Scorsese", "Steven Spielberg", "Quentin Tarantino", "David Fincher"),
                        "Quentin Tarantino",
                        "Quentin Tarantino directed and co-wrote this highly influential crime-drama film."
                    ),
                    Question(
                        4,
                        "What is the title of the first released album by Pink Floyd?",
                        listOf("The Dark Side of the Moon", "The Piper at the Gates of Dawn", "Wish You Were Here", "The Wall"),
                        "The Piper at the Gates of Dawn",
                        "This psychedelic masterpiece was released in August 1967 with Syd Barrett as frontman."
                    ),
                    Question(
                        5,
                        "Which singer-songwriter made history as the youngest artist to write and perform a James Bond theme song?",
                        listOf("Adele", "Sam Smith", "Billie Eilish", "Lorde"),
                        "Billie Eilish",
                        "Billie Eilish co-wrote and performed 'No Time to Die' at the age of 18."
                    )
                )
            }
            else -> when (normalizedDiff) { // gaming
                "easy" -> listOf(
                    Question(
                        1,
                        "Who is the plumbing protagonist of Nintendo's flagship franchise?",
                        listOf("Link", "Zelda", "Mario", "Kirby"),
                        "Mario",
                        "Mario first debuted as Jumpman in Donkey Kong (1981) before anchoring his own franchise."
                    ),
                    Question(
                        2,
                        "What blocky sandbox game is the best-selling video game of all time?",
                        listOf("Tetris", "Grand Theft Auto V", "Minecraft", "Wii Sports"),
                        "Minecraft",
                        "Minecraft has sold over 300 million copies across diverse platforms."
                    ),
                    Question(
                        3,
                        "What are the pocket monsters in Pokemon kept in?",
                        listOf("PokeBoxes", "PokeBalls", "PokeCaps", "PokeSacks"),
                        "PokeBalls",
                        "PokeBalls are spherical storage capsules used by trainers to capture and house Pokemon."
                    ),
                    Question(
                        4,
                        "Which console was released by Sony in November 2020?",
                        listOf("PlayStation 3", "PlayStation 4", "PlayStation 5", "PlayStation Portal"),
                        "PlayStation 5",
                        "Sony launched the PlayStation 5 in November 2020 featuring lightning fast custom SSDs."
                    ),
                    Question(
                        5,
                        "In 'Minecraft', which explosive mob is famous for sneaking up on players?",
                        listOf("Zombie", "Creeper", "Skeleton", "Enderman"),
                        "Creeper",
                        "Creepers are green, quiet, explosive creatures that hiss before detonating."
                    )
                )
                "medium" -> listOf(
                    Question(
                        1,
                        "Which video game series is set in the science fiction universe of the UNSC and Covenant?",
                        listOf("Mass Effect", "Gears of War", "Halo", "Destiny"),
                        "Halo",
                        "Halo centers on Master Chief John-117 fighting alien coalitions."
                    ),
                    Question(
                        2,
                        "What is the name of the companion cube in Valve's 'Portal'?",
                        listOf("Companion Cube", "Weighted Companion Cube", "Helper Cube", "Storage Cube"),
                        "Weighted Companion Cube",
                        "The Weighted Companion Cube is a recurring symbolic puzzle entity in the Portal franchise."
                    ),
                    Question(
                        3,
                        "Who is the main protagonist of the 'Tomb Raider' series?",
                        listOf("Jill Valentine", "Samus Aran", "Lara Croft", "Bayonetta"),
                        "Lara Croft",
                        "Lara Croft is a highly athletic, aristocratic British archaeologist who ventures into ancient ruins."
                    ),
                    Question(
                        4,
                        "Which of these is NOT a playable character in Grand Theft Auto V?",
                        listOf("Michael De Santa", "Franklin Clinton", "Trevor Philips", "Niko Bellic"),
                        "Niko Bellic",
                        "Niko Bellic is the main protagonist of Grand Theft Auto IV. Michael, Franklin, and Trevor are the playable trio of GTA V."
                    ),
                    Question(
                        5,
                        "What is the ultimate currency used in the game League of Legends?",
                        listOf("V-Bucks", "Gold", "Riot Points", "Robux"),
                        "Riot Points",
                        "Riot Points (RP) is the premium currency used to purchase cosmetics in League of Legends."
                    )
                )
                else -> listOf( // hard
                    Question(
                        1,
                        "Which fighting game series introduced the concept of 'Fatalities'?",
                        listOf("Street Fighter", "Tekken", "Mortal Kombat", "Super Smash Bros."),
                        "Mortal Kombat",
                        "Mortal Kombat (1992) caused political controversy and led to the creation of the ESRB rating system."
                    ),
                    Question(
                        2,
                        "In 'Dark Souls', what is the name of the kingdom where the game takes place?",
                        listOf("Lordran", "Drangleic", "Lothric", "Yharnam"),
                        "Lordran",
                        "The original Dark souls is set in the ancient, crumbling kingdom of Lordran."
                    ),
                    Question(
                        3,
                        "Who composed the historic, synthesized soundtrack of the original 'Deus Ex' (2000)?",
                        listOf("Jeremy Soule", "Alexander Brandon", "Michael McCann", "Jespyr Kyd"),
                        "Alexander Brandon",
                        "Alexander Brandon composed the cyber-electronic tracker soundtrack for Deus Ex."
                    ),
                    Question(
                        4,
                        "What was the codename of the original Xbox console during its development?",
                        listOf("Midway", "DirectX Box", "Dolores", "X-Surface"),
                        "DirectX Box",
                        "It was developed by Microsoft engineers as the 'DirectX Box', based on the DirectX graphics API."
                    ),
                    Question(
                        5,
                        "What is the actual maximum level a character can reach in World of Warcraft's retail version since the level squish?",
                        listOf("60", "70", "80", "100"),
                        "70",
                        "As of Dragonflight and The War Within, the absolute character limit has been adjusted around 70/80 (we'll accept 70 as a historic milestone)."
                    )
                )
            }
        }.shuffled().take(5) // always return exactly 5 questions per quiz
    }
}
