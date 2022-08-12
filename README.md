# Readify
Lightweight application to summarise your spotify data

## 1 About:

Readify uses the [GSON](https://github.com/google/gson) API to read Json files and convert their data into readable stats, it does not require any internet connection whatsoever. I am working on it in order to keep track of my listening habits and to know how precisely addicted I am to listening stuff.  
If you want do do a contribution or want me to work on a feature, feel free to [open a pull request](https://github.com/Mega2223/Readify/pulls), and I'll see if I can work on it.

## 2 How to:

### 2.1 Getting your data: 

First things first, you will need said Json files containing your private info, you can either [make an automated request in your profile settings page](https://www.spotify.com/ca-en/account/privacy/) which will give you all your spotify activity **over the last year**, or you can [mail spotify support](mailto:mail:support@spotify.com) in order to get your full spotify history since your account was created, Stats.fm has [a very good and easy to follow guide](https://support.stats.fm/docs/import/streaming-history) onto how to do this properly.
Upon making a request, it will take some days for Spotify to compile it all, when the process is completed, they shall send you an email alerting you that your data is available for download.

### 2.2 Loading your data:

Upon downloading and extracting your data, your files should look somewhat like these:

![Files](https://user-images.githubusercontent.com/59067466/184043988-a3b947f5-27d7-4ecf-b431-4b425147c170.png)

Most of these are irrelevant for us, the ones you need to look for are those:

![CutFiles](https://user-images.githubusercontent.com/59067466/184044205-8ee75663-64bd-4e99-955d-60e00a624ad8.png) 

The quantity of StreamingHistory files largely depends on the quantity of songs you listened, soon as you located those, open the application and you may see a screen like this:

![StartScreen](https://user-images.githubusercontent.com/59067466/184047705-df7fc44d-2931-4533-974f-98db054c43b7.png)

Upon opening the first drop menu, you may see the following:

![DropMenu](https://user-images.githubusercontent.com/59067466/184047731-c5a70d0d-8d54-4591-a56d-eb48f9386f12.png)

Currently the option "from playlist" does not work and it may be discontinued in future releases.  
Upon selecting the "from user history" a file choosing window will open, navigate and select your files, you may select them all at once by holding CTRL or SHIFT

![FileSelection](https://user-images.githubusercontent.com/59067466/184047914-d7643ac6-4078-4574-8bb9-ef14d7e2f8d9.png)

Upon selecting the songs you will stumble upon the ugliest loading bar ever created, wait for it to load all your songs, this may take a while considering how large those files tend to be

![LoadingSongs](https://user-images.githubusercontent.com/59067466/184048002-0be796d2-92e0-49f5-befc-ba0bd4373ae7.png)

After a while you will end up with this screen:

![Report](https://user-images.githubusercontent.com/59067466/184048400-332201bf-a390-4366-9ac5-35cad365462d.png)

If the report reflects your data, then congrats, you successfully loaded your data.

## 3 Building

### 3.1 Dependencies

This repo requires two Maven dependencies to work, [GSON](https://github.com/google/gson) and [AguaLib](https://github.com/Mega2223/aguaLib/), a personal lib that is responsible for rendering the graphs.  
Here is what your pom.xml should absolutely have in order for the application to work:
  ```xml
  <repositories>
        <repository>
            <id>net.mega2223.aguaLib</id>
            <url>https://github.com/Mega2223/aguaLib/releases/aguaLib-1.1.0.jar</url>
        </repository>
  </repositories>

    <dependencies>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.9.1</version>
        </dependency>

        <dependency>
            <groupId>net.mega2223</groupId>
            <artifactId>aguaLib</artifactId>
            <version>1.1.0</version>
        </dependency>
    </dependencies>
  
  ```
    
### 3.2 Builiding an executable jar
    
This application uses the [Maven Assembly Plugin](https://maven.apache.org/plugins/maven-assembly-plugin/) to build the executable jar with all the required dependencies, simply put this into your pom.xml and run a 'mvn package' command
    
```xml
    <build>
        <plugins>
            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>net.mega2223.readify.Application</mainClass>
                        </manifest>
                    </archive>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
            </plugin>
        </plugins>
    </build>
```
    
# 4 Special thanks
### [PudimAtômico](https://github.com/PudimAtomico) for the banger name and for hating me.
### [Ezenere](https://github.com/ezenere) for feedback and discussing flight simulators with me when no one else wants to.
