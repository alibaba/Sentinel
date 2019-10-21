# JVM-SANDBOX

## USER-GUIDE

### Environmental requirements

1. JDK6+
2. Linux／UNIX／MacOS；Temporarily support WINDOWS, mainly some of the script needs to be modified

### Install the container

- **Local installation**

   - First [download](http://gitlab.alibaba-inc.com/jvm-sandbox/jvm-sandbox-doc/raw/master/release/sandbox-stable-bin.zip) latest stable version
   - After downloading, run the `./install-local.sh` script and specify the installation directory for the sandbox。I personally like to install the program in `${HOME}/opt`.

       ```shell
       ./install-local.sh -p ~/opt
       ```
       
       If you can see the following output
       
       ```
       VERSION=0.0.0.i
       PATH=/Users/vlinux/opt/sandbox
       install sandbox successful.
       ```
       
       Congratulations, the installation is complete.

### Start mode

There are two ways to start the sandbox：`ATTACH` or `AGENT`

- **ATTACH**

  Plug and play mode, there is no need to restart the target JVM to complete the sandbox implant. Principle and GREYS, BTrace similar to the use of the JVM Attach mechanism to achieve.

  ```shell
  # Assume that the target JVM process number is`2343`
  ./sandbox.sh -p 2343
  ```
  
  If the output is similar to 
  
  ```
               VERSION : 0.0.0.i
                  MODE : ATTACH
           SERVER_ADDR : localhost
           SERVER_PORT : 49903
        UNSAFE_SUPPORT : ENABLE
          SANDBOX_HOME : /Users/vlinux/opt/sandbox/lib/..
            MODULE_LIB : /Users/vlinux/opt/sandbox/lib/../module
       USER_MODULE_LIB : /Users/vlinux/.sandbox-module
    EVENT_POOL_SUPPORT : ENABLE
    EVENT_POOL_KEY_MIN : 100
    EVENT_POOL_KEY_MAX : 2000
      EVENT_POOL_TOTAL : 3000
  ```

Then the sandbox has been successfully implanted in the target JVM, and opened a port and loaded all modules.

- **AGENT**

  Sometimes we need the sandbox to start before the application code loads, or the larger scale of engineering. This is possible by starting in the `AGENT` mode.
  
  If we assume that `sandbox` is installed in `/Users/vlinux/opt/sandbox`，the following need to be added to the JVM startup parameters
  
  ```shell
  -javaagent:/Users/vlinux/opt/sandbox/lib/sandbox-agent.jar
  ```
  
  When starting the JVM this will load the `sandbox` module before any of the application code is loaded.

### Introduction to Sandbox Engineering

- **Application directory structure**

  ```
  sandbox/
        +--bin/
        |   +--sandbox.sh
        +--cfg/
        |   +--sandbox-logback.xml
        |   +--sandbox.properties
        |   `--version
        +--lib/
        |   +--sandbox-agent.jar
        |   +--sandbox-core.jar
        |   `--sandbox-spy.jar
        `--module/
            `--sandbox-mgr-module.jar
  ```

  - **./sandbox/bin/sandbox.sh**
  
     This is the `sandbox` client script used for starting and managing sandboxes.
     
  - **./sandbox/cfg/**
  
     This directory holds the `sandbox` configuration files. For a detailed list of configuration files, please refer to the following link
     
     - [sandbox-logback.xml](#sandbox-logback.xmlConfiguration)
     - [sandbox.properties](#sandbox.propertiesConfiguration)
     - version

  - **./sandbox/lib/**
    
     This holds `sandbox`s main program and cannot be deleted, renamed or moved!
      
      |File name|What it is|
      |---|---|
      |sandbox-agent.jar|Sandbox start agent|
      |sandbox-core.jar|Sandbox kernel|
      |sandbox-spy.jar|Sandbox spy library<br>Spyware used to provide burnt pits|

  - **./sandbox/module/**

      Sandbox system management module directory, the sandbox itself provides a variety of functions are also through the form of modules to complete.
      
      The `module-mgr` module` sandbox-mgr-module.jar`, which is currently available for module management, is very important and should not be easily removed. You can also replace the management module with your own module management function.
      
      There are more important commonality modules that can be placed here, but if the business is more closely related to the need to frequently load the module recommendations are placed in the `$ {HOME} /. Sandbox-module /` directory to complete. Details of the two directories can be seen [sandbox module directory] (# sandbox module directory)
      
- **Log directory structure**

   The sandbox uses the `logback` log component to complete the logging. The log file is written to the` $ {HOME} / logs / sandbox / sandbox.log` file by default. If you need it, you can also adjust the `sandbox-logback .xml `file to modify the log output configuration.

- **Run-time file**

  After the sandbox starts, a hidden file `$ {HOME} /. Sandbox.token` will be created. This file will complete the interaction between the target JVM process and the sandbox client process.

- **<span id="#Sandbox module directory">Sandbox module directory</span>**

  The sandbox has two catalogs for loading modules, each of which is different.
  
  - `./sandbox/module/`
  
     Sandbox system module directory, used to store the sandbox common management module, such as for the sandbox module management function `module-mgr` module, the future module operation quality monitoring module, security check module will also be stored here , Distributed along with the release of the sandbox.
     
     The system module is not affected by the `sandbox.sh` script's` -f`, `-F` parameter. Only the` -R` parameter allows the sandbox to reload all modules under the system module directory.
     
     The system module does not recommend frequent reloading, and if necessary reload can use the `-R` parameter
  
  - `${HOME}/.sandbox-module/`

     Sandbox user module directory, generally used to store the user self-research module. Self-research modules often face frequent version upgrades that can be done using the `-f` or` -F` parameters when a hot hot swap replacement is required.

 - **Sandbox module**

     - All sandbox modules can be designed for hot swapping
     - A JAR package can declare multiple modules, the module needs to meet the Java SPI specification, requirements

         1. You must have a nonparametric constructor for publish
         2. You must implement the `com.alibaba.jvm.sandbox.api.Module` interface
         3. You must complete the registration in the `META-INF / services / com.alibaba.jvm.sandbox.api.Module` file (Java SPI specification requirements)
        
     - All modules declared in the same JAR package share the same ModuleJarClassLoader   
        
     - There are four states in the module

         - onLoad

            Module is properly loaded by the sandbox, the sandbox will allow the module to normally apply for HTTP, WEBSOCKET and other resources
            
          - unload

             The sandbox will no longer see the module, before the allocation of all resources to the module will be recycled, including the module has been listening to the event of the class will be removed from the interception pile, clean and leave no sequelae.
               
           - onActive
           
              After the module is loaded successfully, the default is frozen state, need to actively activate the code. The module can only listen to the sandbox event in the active state.
               
           - onFreeze

               After the module enters the frozen state, all the sandbox events that are listening before will be blocked. It should be noted that the frozen module will not return the event to listen to the code plug, only `delete ()`, `wathcing ()` or module is unloaded when the plug code will be cleared.

### Configuration file

- ##### <span id="sandbox-logback.xmlConfiguration">sandbox-logback.xml</span>

  Sandbox log frame is used LOGBACK log framework, so the log configuration file is also directly released.
  
  The default log is output to `$ {HOME} / logs / sandbox / sandbox.log`, of course, you can also adjust according to their own needs.
  
  Detailed LOGBACK configuration here do not have long-winded, there is a need for in-depth study can refer to this chapter Bowen [logback common configuration Detailed explanation (order) logback profile] (http://aub.iteye.com/blog/1101222)

- ##### <span id="sandbox.propertiesConfiguration">sandbox.properties</span>

  Sandbox system configuration, where the sandbox of the various system functions to switch settings. Currently open the configuration is not much follow-up with the sandbox function gradually improved, there will be more configuration options open. The current is still in simple terms is the principle of beauty to design.
  
  Here recommended, if there is no special requirements, as far as possible with the default value can be.
  
  |Configuration item|Defaults|Configuration instructions|
  |---|---|---|
  |unsafe.enable|TRUE|Whether to allow the enhanced rt.jar class|
  |event.pool.enable|TRUE|Whether the event object pool is enabled|
  |event.pool.key.min|100|The event object pool holds the minimum value for each event|
  |event.pool.key.max|2000|The event object pool holds the maximum value for each event|
  |event.pool.total|3000|Event Object Pool Total Event Hold Value|
  
  

### Command description

`sandbox.sh` is the main operation of the main sandbox client, of course, if you are interested can write a script to complete their own,

`sandbox.sh` and sandbox mainly through the HTTP protocol to complete the communication, so the Linux system must be installed `curl` order.

- `-h`

  Output help information
  
- `-p`

  Specifies the target JVM process number<br>
  Operation, you only need to specify the corresponding JVM process number, do not care about the underlying binding of the sandbox HTTP port, `sandbox.sh` script will help you get this matter
  
- `-v`

  Outputs the sandbox version information loaded into the target JVM
  
- `-l`

  Lists the modules already loaded in the target JVM sandbox
  
- `-F`

  Forced to refresh the user module (`$ {HOME} /. Sandbox-module /` directory of the module).
  
  - First uninstall all the loaded user modules and then reload them
  - When any module fails to load, ignore the module and continue loading other loadable modules
  - When the module is unloaded, it will release the sandbox as a resource for the module

     - HTTP link
     - WEBSOCKET link
     - ModuleJarClassLoader
     - The event is caused by the unloading of the module

- `-f`

   Refresh the user module, the steps are like `-F` parameters. But when any one module fails to load, the current operation of the refresh, the current failure of the module and the module to be loaded will not continue to be loaded.
   
- `-R`

  Sandbox reset，when the sandbox is reset, all system modules `/ sandbox / module /` and all user modules `$ {HOME} /. Sandbox-module /` will be forced to refresh all system modules.
  
- `-u`

  Unloads the specified module to support wildcard expression subkeys. The uninstall module does not distinguish between the system module and the user module. All modules can be uninstalled with this parameter, so remember not to uninstall `module-mgr`, otherwise you will lose the module management function.
  
  > EXAMPLE
  > 
  > ```
  > # The target JVM process number is 4321，The modules that need to be uninstalled are named `debug-module`
  > ./sandbox.sh -p 4321 -u 'debug-module'
  >
  > # You can also use wildcards
  > ./sandbox.sh -p 4321 -u 'debug-*'
  > ```
  
- `-a`

  Activate the module, the module can be activated by the sandbox event
  
- `-A`

  Freeze the module, the module will not be aware of any sandbox after the freeze, but the corresponding code is still in the pile.
  
- `-m`

  To view the module details, the module name needs to match exactly and does not support wildcards.
  
  > EXAMPLE
  >
  > ```
  > # The target JVM process number is 4321，The module to be observed is named `module-mgr`
  > ./sandbox.sh -p 4321 -m 'module-mgr'
  >       ID : module-mgr
  >  VERSION : 0.0.0.1
  >   AUTHOR : luanjia@taobao.com
  > JAR_FILE : /Users/vlinux/opt/sandbox/lib/../module/sandbox-mgr-module.jar
   >    STATE : FROZEN
    >     MODE : {AGENT,ATTACH}
   >    CLASS : class com.alibaba.jvm.sandbox.module.mgr.ModuleMgrModule
  >   LOADER : ModuleJarClassLoader[crc32=1721245995;file=/Users/vlinux/opt/sandbox/lib/../module/sandbox-mgr-module.jar;]
    >     cCnt : 0
    >     mCnt : 0
  > ```
