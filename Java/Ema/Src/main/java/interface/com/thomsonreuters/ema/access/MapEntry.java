///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2015. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.access;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * MapEntry represents an entry of Map.
 * <p>MapEntry associates entry's key, permission information, action, data and its data type.</p>
 */
public interface MapEntry
{
	/**
	 * A MapAction represents a map entry action.
	 */
	public static class MapAction
	{
		/**
		 * Indicates a partial change of the current Omm data.
		 */
		public static final int UPDATE = 1;

		/**
		 * Indicates to append or replace the current Omm data.
		 */
		public static final int ADD = 2;

		/**
		 * Indicates to remove current Omm data.
		 */
		public static final int DELETE = 3;
	}

	/**
	 * Returns the MapAction value as a string format.
	 * 
	 * @return String containing string representation of MapAction
	 */
	public String mapActionAsString();

	/**
	 * Returns the DataType of the entry's load.
	 * Return of {@link com.thomsonreuters.ema.access.DataType.DataTypes#NO_DATA} signifies no data present in load.
	 * Return of {@link com.thomsonreuters.ema.access.DataType.DataTypes#ERROR}
	 * signifies error while extracting content of load
	 * 
	 * @return data type of the contained object
	 */
	public int loadType();

	/**
	 * Returns a string representation of the class instance.
	 * 
	 * @return string representation of the class instance
	 */
	public String toString();

	/**
	 * Indicates presence of PermissionData.
	 * 
	 * @return true if permission data is set; false otherwise
	 */
	public boolean hasPermissionData();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.ReqMsg}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.ReqMsg} class reference to contained object
	 */
	public ReqMsg reqMsg();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.RefreshMsg}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.RefreshMsg} class reference to contained entry's load object
	 */
	public RefreshMsg refreshMsg();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.UpdateMsg}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.UpdateMsg} class reference to contained entry's load object
	 */
	public UpdateMsg updateMsg();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.StatusMsg}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.StatusMsg} class reference to contained entry's load object
	 */
	public StatusMsg statusMsg();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.PostMsg}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.PostMsg} class reference to contained entry's load object
	 */
	public PostMsg postMsg();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.AckMsg}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.AckMsg} class reference to contained entry's load object
	 */
	public AckMsg ackMsg();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.GenericMsg}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.GenericMsg} class reference to contained entry's load object
	 */
	public GenericMsg genericMsg();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.FieldList}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.FieldList} class reference to contained entry's load object
	 */
	public FieldList fieldList();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.ElementList}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.ElementList} class reference to contained entry's load object
	 */
	public ElementList elementList();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.Map}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.Map} class reference to contained entry's load object
	 */
	public Map map();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.Vector}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.Vector} class reference to contained entry's load object
	 */
	public Vector vector();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.Series}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.Series} class reference to contained entry's load object
	 */
	public Series series();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.FilterList}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.FilterList} class reference to contained entry's load object
	 */
	public FilterList filterList();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.OmmOpaque}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.OmmOpaque} class reference to contained entry's load object
	 */
	public OmmOpaque opaque();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.OmmXml}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.OmmXml} class reference to contained entry's load object
	 */
	public OmmXml xml();

	/**
	 * Returns the current OMM data represented as a specific complex type.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.OmmAnsiPage}
	 * @return {@link com.thomsonreuters.ema.access.OmmAnsiPage} class reference to contained entry's load object
	 */
	public OmmAnsiPage ansiPage();

	/**
	 * Returns the contained Data based on the DataType.
	 * 
	 * @return Data class reference to contained entry's load object
	 */
	public Data load();

	/**
	 * Returns the contained key Data based on the key DataType.
	 * 
	 * @return Key class reference to contained entry's Key object
	 */
	public Key key();

	/**
	 * Returns the current action on the OMM data.
	 * 
	 * @return MapAction
	 */
	public int action();

	/**
	 * Returns PermissionData.
	 * 
	 * @throws OmmInvalidUsageException if {@link #hasPermissionData()} returns false
	 * 
	 * @return ByteBuffer containing permission information
	 */
	public ByteBuffer permissionData();

	/**
	 * Returns Error.
	 * 
	 * @throws OmmInvalidUsageException if contained object is not {@link com.thomsonreuters.ema.access.OmmError}
	 * 
	 * @return {@link com.thomsonreuters.ema.access.OmmError} class reference to contained entry's load object
	 */
	public OmmError error();
	
	// new code
	
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key containing long key information
		 * @param action specifies action to be applied to the entry
		 * @param value specifies complex type associated with this entry
		 * @return reference to this object
		 */
		public MapEntry keyInt(long key, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key containing long key information
		 * @param action specifies action to be applied to the entry
		 * @param value specifies complex type associated with this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyInt(long key, int action, ComplexType value, ByteBuffer permissionData);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key containing UInt key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyUInt(long key, int action, ComplexType value);		
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key containing UInt key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyUInt(long key, int action, ComplexType value, ByteBuffer permissionData);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key containing UInt key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyUInt(BigInteger key, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key containing UInt key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyUInt(BigInteger key, int action, ComplexType value, ByteBuffer permissionData);									

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param mantissa specifies OmmReal mantissa part of key information
		 * @param magnitudeType specifies OmmReal magnitudeType part of key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyReal(long mantissa, int magnitudeType, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param mantissa specifies OmmReal mantissa part of key information
		 * @param magnitudeType specifies OmmReal magnitudeType part of key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyReal(long mantissa, int magnitudeType, int action, ComplexType value, ByteBuffer permissionData);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key specifies double to be converted to {@link com.thomsonreuters.ema.access.OmmReal}
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyReal(double key, int action, ComplexType value);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key specifies double to be converted to {@link com.thomsonreuters.ema.access.OmmReal}
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to  this entry
		 * @return reference to this object
		 */
		public MapEntry keyReal(double key,  int action, ComplexType value, ByteBuffer permissionData);

		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key specifies double to be converted to {@link com.thomsonreuters.ema.access.OmmReal}
		 * @param magnitudeType OmmReal magnitudeType used for the conversion
		 *        (default value is {@link com.thomsonreuters.ema.access.OmmReal.MagnitudeType#EXPONENT_0})
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyReal(double key,  int magnitudeType, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key specifies double to be converted to {@link com.thomsonreuters.ema.access.OmmReal}
		 * @param magnitudeType OmmReal magnitudeType used for the conversion
		 *            (default value is {@link com.thomsonreuters.ema.access.OmmReal.MagnitudeType#EXPONENT_0})
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to  this entry
		 * @return reference to this object
		 */
		public MapEntry keyReal(double key,  int magnitudeType, int action, ComplexType value, ByteBuffer permissionData);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key containing float key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyFloat(float key, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key containing float key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyFloat(float key, int action, ComplexType value, ByteBuffer permissionData);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 * @param key containing double key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyDouble(double key, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key containing double key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyDouble(double key, int action, ComplexType value, ByteBuffer permissionData);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 * @throws OmmOutOfRangeException if passed in OmmDate is invalid
		 * 
		 * @param year specifies OmmDate year part of key information (0 - 4095)
		 * @param month specifies OmmDate month part of key information (0 - 12)
		 * @param day specifies OmmDate day part of key information (0 - 31)
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyDate(int year, int month, int day, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 * @throws OmmOutOfRangeException if passed in OmmDate is invalid
		 * 
		 * @param year specifies OmmDate year part of key information (0 - 4095)
		 * @param month specifies OmmDate month part of key information (0 - 12)
		 * @param day specifies OmmDate day part of key information (0 - 31)
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyDate(int year, int month, int day, int action, ComplexType value, ByteBuffer permissionData);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 * @throws OmmOutOfRangeException if passed in OmmTime is invalid
		 * 
		 * @param hour specifies OmmTime hour part of key information (0 - 23)
		 * @param minute specifies OmmTime minute part of key information (0 - 59)
		 * @param second specifies OmmTime second part of key information (0 - 60)
		 * @param millisecond specifies OmmTime millisecond part of key information (0 - 999)
		 * @param microsecond specifies OmmTime microsecond part of key information (0 - 999)
		 * @param nanosecond specifies OmmTime nanosecond part of key information (0 - 999)
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyTime(int hour, int minute, int second, int millisecond, int microsecond, int nanosecond, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 * @throws OmmOutOfRangeException if passed in OmmTime is invalid
		 * 
		 * @param hour specifies OmmTime hour part of key information (0 - 23)
		 * @param minute specifies OmmTime minute part of key information (0 - 59)
		 * @param second specifies OmmTime second part of key information (0 - 60)
		 * @param millisecond specifies OmmTime millisecond part of key information (0 - 999)
		 * @param microsecond specifies OmmTime microsecond part of key information (0 - 999)
		 * @param nanosecond specifies OmmTime nanosecond part of key information (0 - 999)
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyTime(int hour, int minute, int second, int millisecond, int microsecond, int nanosecond, int action, ComplexType value, ByteBuffer permissionData);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 * @throws OmmOutOfRangeException if passed in OmmDateTime is invalid
		 * 
		 * @param year specifies OmmDateTime year part of key information (0 - 4095)
		 * @param month specifies OmmDateTime month part of key information (0 - 12)
		 * @param day specifies OmmDateTime day part of key information (0 - 31)
		 * @param hour specifies OmmDateTime hour part of key information (0 - 23)
		 * @param minute specifies OmmDateTime minute part of key information (0 - 59)
		 * @param second specifies OmmDateTime second part of key information (0 - 60)
		 * @param millisecond specifies OmmDateTime millisecond part of key information (0 - 999)
		 * @param microsecond specifies OmmDateTime microsecond part of key information (0 - 999)
		 * @param nanosecond specifies OmmDateTime nanosecond part of key information (0 - 999)
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyDateTime(int year, int month, int day, int hour, int minute, int second, int millisecond, int microsecond, int nanosecond, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 * @throws OmmOutOfRangeException if passed in OmmDateTime is invalid
		 * 
		 * @param year specifies OmmDateTime year part of key information (0 - 4095)
		 * @param month specifies OmmDateTime month part of key information (0 - 12)
		 * @param day specifies OmmDateTime day part of key information (0 - 31)
		 * @param hour specifies OmmDateTime hour part of key information (0 - 23)
		 * @param minute specifies OmmDateTime minute part of key information (0 - 59)
		 * @param second specifies OmmDateTime second part of key information (0 - 60)
		 * @param millisecond specifies OmmDateTime millisecond part of key information (0 - 999)
		 * @param microsecond specifies OmmDateTime microsecond part of key information (0 - 999)
		 * @param nanosecond specifies OmmDateTime nanosecond part of key information (0 - 999)
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyDateTime(int year, int month, int day, int hour, int minute, int second, int millisecond, int microsecond, int nanosecond, int action, ComplexType value, ByteBuffer permissionData);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param timeliness specifies {@link com.thomsonreuters.ema.access.OmmQos.Timeliness} part of key information
		 * @param rate specifies {@link com.thomsonreuters.ema.access.OmmQos.Rate} part of key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyQos(int timeliness, int rate, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param timeliness specifies {@link com.thomsonreuters.ema.access.OmmQos.Timeliness} part of key information
		 * @param rate specifies {@link com.thomsonreuters.ema.access.OmmQos.Rate} part of key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to  this entry
		 * @return reference to this object
		 */
		public MapEntry keyQos(int timeliness, int rate, int action, ComplexType value, ByteBuffer permissionData);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param streamState specifies {@link com.thomsonreuters.ema.access.OmmState.StreamState} part of key information
		 * @param dataState specifies {@link com.thomsonreuters.ema.access.OmmState.DataState} part of key information
		 * @param statusCode specifies {@link com.thomsonreuters.ema.access.OmmState.StatusCode} part of key information
		 * @param statusText specifies OmmState text part of key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyState(int streamState, int dataState, int statusCode, String statusText, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param streamState specifies {@link com.thomsonreuters.ema.access.OmmState.StreamState} part of key information
		 * @param dataState specifies {@link com.thomsonreuters.ema.access.OmmState.DataState} part of key information
		 * @param statusCode specifies {@link com.thomsonreuters.ema.access.OmmState.StatusCode} part of key information
		 * @param statusText specifies OmmState text part of key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyState(int streamState, int dataState, int statusCode, String statusText, int action, ComplexType value, ByteBuffer permissionData);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key int containing Enum key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyEnum(int key, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key int containing Enum key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyEnum(int key, int action, ComplexType value, ByteBuffer permissionData);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key ByteBuffer containing Buffer key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyBuffer(ByteBuffer key, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key ByteBuffer containing Buffer key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyBuffer(ByteBuffer key, int action, ComplexType value, ByteBuffer permissionData);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key String containing Ascii key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyAscii(String key, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key String containing Ascii key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyAscii(String key, int action, ComplexType value, ByteBuffer permissionData);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key ByteBuffer containing Utf8 key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyUtf8(ByteBuffer key, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key ByteBuffer containing Utf8 key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyUtf8(ByteBuffer key, int action, ComplexType value, ByteBuffer permissionData);

		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key ByteBuffer containing Rmtes key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @return reference to this object
		 */
		public MapEntry keyRmtes(ByteBuffer key, int action, ComplexType value);
		
		/**
		 * Adds complex OMM data identified by a specific simple type of OMM data.
		 * 
		 * @throws OmmInvalidUsageException if an error is detected (exception will
		 *        specify the cause of the error)
		 *        
		 * @param key ByteBuffer containing Rmtes key information
		 * @param action specifies action to apply to the entry
		 * @param value complex type contained in this entry
		 * @param permissionData ByteBuffer containing permission data related to this entry
		 * @return reference to this object
		 */
		public MapEntry keyRmtes(ByteBuffer key, int action, ComplexType value, ByteBuffer permissionData);

}