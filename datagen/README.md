This is a data set generator for use with Google's machine learning API.  It is derived from Harry Schwartz's Markov Sentence Generator.  

- It generates data sets in CSV format.  
- It also generates data sets that can be used with integer column values and categorical models (as opposed to regression models).  
- The Google API automatically uses regression models upon identifying integer column values, so I've used a simple hack along with the java code in `replaceNumbers()` in `prediction-java` to convert numbers to and from English.  

## To run the script:

`$ ./data-generator.py filename [chain length]`

- `filename` is a file containing training text
- `chain length` represents the number of words taken into account when choosing the next word.  
- Chain length defaults to 1 (which is fastest), but increasing this may generate more realistic text, albeit slightly more slowly.  

The surgery text and the Conan text are taken from Project Gutenberg.  The usual copyright headers had to be removed so that they could serve as useful sample input, but naturally all the rights and restrictions of a Gutenberg book still apply.
