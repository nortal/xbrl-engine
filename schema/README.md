#XBRL Schema

XBRL schema consists of taxonomy and metadata build against it.

You can download latest IFRS taxonomy from:
http://www.ifrs.org/issued-standards/ifrs-taxonomy/

Unzip and place in resource/schema
Now select the desired `Entry Point` file and create a copy named schema.xsd.
Open the schema.xsd file and add following **attributes** to the `xsd:schema` tag:

```
xmlns:iso4217="http://www.xbrl.org/2003/iso4217"
xmlns:xbrldi="http://xbrl.org/2006/xbrldi"
xmlns:nonnum="http://www.xbrl.org/dtr/type/non-numeric"
xmlns:num="http://www.xbrl.org/dtr/type/numeric"
```

also add these line before closing `xsd:schema` tag at the end of file:

```xml
<xsd:import namespace="http://www.xbrl.org/2003/iso4217" schemaLocation="http://www.xbrl.org/2003/iso4217-2003-05-16.xsd"/>
<xsd:import namespace="http://xbrl.org/2006/xbrldi" schemaLocation="http://www.xbrl.org/2006/xbrldi-2006.xsd"/>
<xsd:import namespace="http://www.xbrl.org/dtr/type/numeric" schemaLocation="http://www.xbrl.org/dtr/type/numeric-2009-12-16.xsd"/>
<xsd:import namespace="http://www.xbrl.org/dtr/type/non-numeric" schemaLocation="http://www.xbrl.org/dtr/type/nonNumeric-2009-12-16.xsd"/>
```

After that you will have to run the generator against it and copy generated metadata.xml file to resource folder, please refer to the README in the generator project for details on how to run it.

Now you are ready to build this project. 
During build process the xjc task will generate java source against the schema.xsd file.

Note: If you are updating XBRL taxonomy version, also be sure to update `metamodel\src\main\java\com\nortal\xbrl\metamodel\namespace\package-info.java`
