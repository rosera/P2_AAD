# PopularMovies
Popular Movies - This is the repository for Stage 2 of the Udacity Android Associate Developer Nanodegree

## Getting Started

Popular Movies provides an interface for the TMDB movie API. Note: This Android application supports both tablets and phones. Information presented is consumeed via the TMDB API and presented in a simple two screen UX.

Popular Movies Image

Tablet Version (Pixel C)

![Popular Movies tablet application](screenshots/pixelc_screenshot0_med.png?raw=true "Stage 2")

![Popular Movies tablet application](screenshots/pixelc_screenshot1_med.png?raw=true "Stage 2")


## Application architecture (generated using the quickwindiagram tool)
TBD


Libraries
  +Picasso
  +Volley



### Prerequisites

The project requires the Volley library and a valid TMDB API key to compile and run. A valid TMDB API key can be accessed via the TMDB website.

The Volley library has been cloned at the same directory level as the P1_AAD project.
Note:
  Volley has now been moved to the following location: https://github.com/google/volley

i.e.

+ P2_AAD
  + app
  + build
  + gradle
  + screenshots
  + volley


### How to build

1. git clone https://github.com/rosera/P2_AAD.git
2. cd P2_AAD
3. git clone https://github.com/google/volley.git (i.e. clone into the existing directory in P2_AAD)
4. Start Android Studio
5. Import the P2_AAD project
6. Create/Amend a new gradle file - gradle.properties
7. Edit gradle.properties and add TMDB_API_KEY="Enter Your valid API KEY" (Note: if you dont already have a valid TMDB API key, sign up at https://developers.themoviedb.org/3/getting-started).

![Popular Movies phone application](screenshots/gradle-properties-screenshot.png?raw=true "Gradle Properties")

8. Compile and run the code

## Acknowledgments

* Massive thanks to the Udacity coaches and fellow students for the help and support
* Fellow students on AAD for keeping me motivated
