# Elektron SDK - Java Edition
This is the Elektron SDK. This SDK is an all encompassing package of all Elektron APIs. This currently includes the Elektron Message API (EMA) and the Elektron Transport API (ETA).

The **Elektron Message API (EMA)** is an ease of use, open source, OMM API. EMA is designed to provide clients rapid development of applications, minimizing lines of code and providing a broad range of flexibility. It provides flexible configuration with default values to simplify use and deployment. EMA is written on top of the Elektron Transport API (ETA) utilizing the Value Added Reactor and Watchlist.

The **Elektron Transport API (ETA)** is the re-branded Ultra Performance API (UPA). ETA is Thomson Reuters low-level Transport and OMM encoder/decoder API. It is used by the Thomson Reuters Enterprise Platform for Real Time and Elektron for the optimal distribution of OMM/RWF data and allows applications to achieve the highest performance, highest throughput, and lowest latency. ETA fully supports all OMM constructs and messages.

(C) Copyright 2018 - 2019 Thomson Reuters Limited. All rights reserved,
Reuters Oak Brook, IL USA

# New In This Release

Please refer to the CHANGELOG file in this section to see what is new in this release of Elektron SDK - Java Edition. Also in CHANGELOG is a list of issues fixed in this release and a history of features and fixes introduced per released version. 

### Supported Platforms, OSs, Compilers

#### Hardware/OS Requirements

- HP Intel PC or AMD Opteron (64-bit)
- CPUs must have high resolution timer frequencies greater than 1GHz.

#### Supported Java Version 
The Elektron-SDK supports JDK 1.7, 1.8 & 1.9. 

Thomson Reuters fully supports the use of the EMA Java Edition developers kit on the core linux and windows platforms listed below.

Thomson Reuters will extend support to other platforms based on the following criteria:
- EMA Java is used with a J2SE 7,8 or 9 compliant JVM
- All problems must be reproducible on one of the core platforms listed below. Thomson Reuters support teams will only be able to reproduce problems on the core platforms.

#### Supported Platforms
The Elektron-SDK provides support for multicast connections using JNI libraries. Also included are closed source libraries for reliable multicast support and value add cache. These libraries are available for the following platform and compiler combinations:

##### Windows

Platforms:

	Windows Server 2008 Enterprise Edition or later 64-bit
	Windows Server 2012 Enterprise Edition or later 64-bit
	Windows Server 2016 Enterprise Edition or later 64-bit
	Microsoft Windows 7 Professional or later 64-bit
	Microsoft Windows 8 Professional or later 64-bit
	Microsoft Windows 8.1 Professional or later 64-bit
	Microsoft Windows 10 Professional 64-bit

Compilers (only on OSs supported by Microsoft):

	Microsoft Visual Studio 11.0 (2012) 64-bit (JNI Libraries)
	Microsoft Visual Studio 12.0 (2013) 64-bit (JNI Libraries)
	Microsoft Visual Studio 14.0 (2015) 64-bit (JNI Libraries)
	Microsoft Visual Studio 15.0 (2017) 64-bit (JNI Libraries)

##### Linux

Platforms:

	Red Hat Enterprise Linux 6.X Release 64bit, GCC 4.4.4 (JNI Libraries)
	Oracle Linux Server 7.X 64-bit, GCC 4.8.2 (JNI Libraries)

#### Tested Versions

This release has been tested with the following:

	Oracle Java SE 7 (JDK1.7)
	Oracle Java SE 8 (JDK1.8)
	Oracle Java SE 9 (JDK1.9)

#### Proxy Authentication Support

Proxies:

- Microsoft's Forefront
- [Free Proxy](http://www.handcraftedsoftware.org/)

Authentication Schemes:

- Basic
- NTLM
- NEGOTIATE/Kerberos

#### Encryption Support

This release supports encryption for TLS 1.0, TLS 1.1 and TLS 1.2.  

**Future Deprecation Notice:** 
In the next major release of ESDK, support for TLS 1.0 will be dropped.

##### Generating a keystore file
The **keystore** file is used to contain your own private keys and public key certificates
which is used for SSL/TLS handshake with server certificates to create an encrypted connection type. 
The following is the steps to create a keystore file and import a intermediate certificate from DigiCert.
- Download the DigiCert SHA2 Secure Server CA certificate at https://dl.cacerts.digicert.com/DigiCertSHA2SecureServerCA.crt
- Use the Java **keytool** tool to import the certificate into an existing or a new Java keystore file below.
  keytool -import -alias digicert -keystore <keystore filename> -storepass <keystore password> -file DigiCertSHA2SecureServerCA.crt

### Interoperability

ESDK Java supports connectivity to the following platforms:

- Enterprise Platform for Real-Time (RSSL/RWF connections) : ADS version 2.6 and higher, ADH version 2.6 and higher. 
- Elektron: Elektron Deployed, Elektron Hosted, Elektron Direct Feed

NOTE: Connectivity to RDF-Direct is supported for Level 1 and Level 2 data.

This release has been tested with the following:

- ADS 3.3.0
- ADH 3.3.0
- DACS 7.2

# Documentation
  
Please refer to top level README.md and to Java/Eta/README.MD or Java/Ema/README.MD files to find more information. In this directory, please find the test plan with test results: ESDK-Java-Edition\_Test\_Plan.xlsx

# Installation & Build

Please refer to Installation Guides located in Cpp-C/Eta/Docs and Cpp-C/Ema/Docs for detailed instructions. In this section are some basic details.

## Common Setup
This section shows the required setup needed before you can build any of the Java APIs.

Firstly, obtain the source from this repository. It will contain all of the required source to build ESDK as detailed below. In addition, this repository depends on the [Elektron-SDK-BinaryPack](http://www.github.com/Refinitiv/Elektron-SDK-BinaryPack) repository and pulls the ETA libraries from that location. The BinaryPack contains libraries for the closed source portions of the product, permitting users to build and link all dependent libraries to have a fully functional product. 

## Building ESDK

**Using Gradle**:

Gradle is now used to build ESDK.
Gradle can be downloaded from https://gradle.org

Refer to the ESDK Java Installation Guide for more detailed Gradle build instructions than what is described below.

Navigate to `Elektron-SDK/Java` and issue the appropriate Gradle command as follows:

	Windows: gradlew.bat jar
	Linux: ./gradlew jar
	
	This command builds the jar files.

#### Running examples

To run an example, issue the appropriate command as follows:
	  
	Windows: gradlew.bat runExampleName [-PcommandLineArgs="arguments"]
	Linux: ./gradlew runExampleName [-PcommandLineArgs="arguments"]
	(where runExampleName is the name of the example to run and arguments are the example arguments, only ETA supports the use of arguments)
	 
Issue the following command to get a list of all example names.
	  
	ETA Windows: gradlew.bat Eta:Applications:Examples:tasks --all
	ETA Linux: ./gradlew Eta:Applications:Examples:tasks --all
	EMA Windows: gradlew.bat Ema:Examples:tasks --all
	EMA Linux: ./gradlew Ema:Examples:tasks --all

ETA example, the following command runs the VAConsumer example.
		
	Windows: gradlew.bat runVaConsumer -PcommandLineArgs="-c localhost:14002 DIRECT_FEED mp:TRI"
	Linux: ./gradlew runVaConsumer -PcommandLineArgs="-c localhost:14002 DIRECT_FEED mp:TRI"

EMA example, the following command runs the example270__SymbolList example.
		
	Windows: gradlew.bat runconsumer270
	Linux: ./gradlew runconsumer270

#### Running examples with Debug

To debug a encrypted consumer connection, you can add the following JVM argument:

	-Djavax.net.debug=all

This provides SSL/TLS details that can be useful if SSL/TLS handshake failed


# Obtaining the Thomson Reuters Field Dictionaries

The Thomson Reuters `RDMFieldDictionary` and `enumtype.def` files are present in this GitHub repo under `Java/etc`. In addition, the most current version can be downloaded from the Customer Zone from the following location.

https://customers.reuters.com/a/technicalsupport/softwaredownloads.aspx

- **Category**: MDS - General
- **Products**: TREP Templates Service Pack

# Maven Central

For ease of product use, as of the ESDK 1.2 release, Thomson Reuters maintains its ESDK Jar files on Maven Central.

You can download ESDK libraries and dependencies from Maven Central using several different tools, specific
procedural instructions are not included here. Maven uses the following syntax to specify ESDK dependencies:

	<dependency>
		<groupId>com.thomsonreuters.ema</groupId>
		<artifactId>ema</artifactId>
		<version>3.3.0.0</version>
	</dependency>

	<dependency>
		<groupId>com.thomsonreuters.upa</groupId>
		<artifactId>upa</artifactId>
		<version>3.3.0.0</version>
	</dependency>

	<dependency>
		<groupId>com.thomsonreuters.upa.valueadd</groupId>
		<artifactId>upaValueAdd</artifactId>
		<version>3.3.0.0</version>
	</dependency>

	<dependency>
		<groupId>com.thomsonreuters.upa.valueadd.cache</groupId>
		<artifactId>upaValueAddCache</artifactId>
		<version>3.3.0.0</version>
	</dependency>

	<dependency>
		<groupId>com.thomsonreuters.upa.ansi</groupId>
		<artifactId>ansipage</artifactId>
		<version>3.3.0.0</version>
	</dependency>

Gradle uses the following syntax to specify ESDK dependencies:

	compile group: 'com.thomsonreuters.ema', name: 'ema', version: '3.3.0.0'
	compile group: 'com.thomsonreuters.upa', name: 'upa', version: '3.3.0.0'
	compile group: 'com.thomsonreuters.upa.valueadd', name: 'upaValueAdd', version: '3.3.0.0'
	compile group: 'com.thomsonreuters.upa.valueadd.cache', name: 'upaValueAddCache', version: '3.3.0.0'                    
	compile group: 'com.thomsonreuters.upa.ansi', name: 'ansipage', version: '3.3.0.0'  

# Developing 

If you discover any issues with this project, please feel free to create an Issue.

If you have coding suggestions that you would like to provide for review, please create a Pull Request.

We will review issues and pull requests to determine any appropriate changes.


# Contributing
In the event you would like to contribute to this repository, it is required that you read and sign the following:

- [Individual Contributor License Agreement](https://github.com/Refinitiv/Elektron-SDK/blob/master/Elektron%20API%20Individual%20Contributor%20License%20Agreement.pdf)
- [Entity Contributor License Agreement](https://github.com/Refinitiv/Elektron-SDK/blob/master/Elektron%20API%20Entity%20Contributor%20License%20Agreement.pdf)


Please email a signed and scanned copy to sdkagreement@thomsonreuters.com.  If you require that a signed agreement has to be physically mailed to us, please email the request for a mailing address and we will get back to you on where you can send the signed documents.


# Notes:
- For more details on each API, please see the corresponding readme file in their top level directory.
- This section contains APIs that are subject to proprietary and open source licenses.  Please make sure to read the readme files within each API flavor directory for clarification.
- Please make sure to review the LICENSE.md file.
- Java unit tests may use [Mockito](http://site.mockito.org/) for creation of mock objects. Mockito is distributed under the MIT license.
