/*|-----------------------------------------------------------------------------
 *|            This source code is provided under the Apache 2.0 license      --
 *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
 *|                See the project's LICENSE.md for details.                  --
 *|           Copyright Thomson Reuters 2015. All rights reserved.            --
 *|-----------------------------------------------------------------------------
 */

#ifndef __thomsonreuters_eoa_foundation_statusinfo_h
#define __thomsonreuters_eoa_foundation_statusinfo_h

/**
	@class thomsonreuters::eoa::foundation::StatusInfo StatusInfo.h "Foundation/Include/StatusInfo.h"
	@brief StatusInfo provides access to the information brought in by a status message.

	\remark All methods in this class are \ref SingleThreaded.

	@see Node,
		EoaString,
		EoaBuffer,
		OmmInvalidUsageException,
		OmmMemoryExhaustionException
*/

#include "Foundation/Include/Common.h"

namespace thomsonreuters {

namespace eoa {

namespace foundation {

class EoaBuffer;
class EoaString;
class OmmState;
class Node;
class StatusInfoImpl;
class ConsumerServiceImpl;

class EOA_FOUNDATION_API StatusInfo
{
public:

	///@name Accessors
	//@{
	/** Indicates presence of the Name.
		@return true if name is set; false otherwise
	*/
	bool hasName() const throw();

	/** Indicates presence of the NameType.
		@return true if name type is set; false otherwise
	*/
	bool hasNameType() const throw();

	/** Indicates presence of the ServiceId.
		@return true if service id is set; false otherwise
	*/
	bool hasServiceId() const throw();

	/** Indicates presence of the ServiceName.
		@return true if service name is set; false otherwise
	*/
	bool hasServiceName() const throw();

	/** Indicates presence of the Identifier.
		@return true if Id is set; false otherwise
	*/
	bool hasId() const throw();

	/** Indicates presence of the Filter.
		@return true if filter is set; false otherwise
	*/
	bool hasFilter() const throw();

	/** Indicates presence of the ExtendedHeader.
		@return true if extendedHeader is set; false otherwise
	*/
	bool hasExtendedHeader() const throw();
	
	/** Indicates presence of ItemGroup.
		@return true if Item Group information is set; false otherwise
	*/
	bool hasItemGroup() const throw();

	/** Indicates presence of State.
		@return true if state information is set; false otherwise
	*/
	bool hasState() const throw();

	/** Indicates presence of PermissionData.
		@return true if permission data is set; false otherwise
	*/
	bool hasPermissionData() const throw();

	/** Indicates presence of PublisherId.
		@return true if publisher id is set; false otherwise
	*/
	bool hasPublisherId() const throw();

	/** Returns the StreamId, which is the unique stream identifier on the wire.
		@return stream id value
	*/
	Int32 getStreamId() const throw();

	/** Returns the DomainType, which is the unique identifier of a domain.
		@return domain type value
	*/
	UInt16 getDomainType() const throw();

	/** Returns the Name.
		@throw OmmInvalidUsageException if hasName() returns false
		@return EoaString containing name
	*/
	const EoaString& getName() const;

	/** Returns the NameType.
		@throw OmmInvalidUsageException if hasNameType() returns false
		@return name type value
	*/
	UInt8 getNameType() const;

	/** Returns the ServiceId.
		@throw OmmInvalidUsageException if hasServiceId() returns false
		@return service id value
	*/
	UInt32 getServiceId() const;

	/** Returns the ServiceName.
		@throw OmmInvalidUsageException if hasServiceName() returns false
		@return EoaString containing service name
	 */
	const EoaString& getServiceName() const;

	/** Returns the Identifier.
		@throw OmmInvalidUsageException if hasId() returns false
		@return id value
	*/
	Int32 getId() const;

	/** Returns the Filter.
		@throw OmmInvalidUsageException if hasFilter() returns false
		@return filter value
	*/
	UInt32 getFilter() const;

	/** Returns the ExtendedHeader.
		@throw OmmInvalidUsageException if hasExtendedHeader() returns false
		@return EoaBuffer containing extendedHeader info value
	*/
	const EoaBuffer& getExtendedHeader() const;

	/** Returns the contained attribute
		@return reference to Node object
	*/
	const Node& getAttrib() const throw();

	/** Returns the contained payload.
		@return reference to Node object
	*/
	const Node& getPayload() const throw();

	/** Returns State.
		@throw OmmInvalidUsageException if hasState() returns false
		@return state of item
	*/
	const OmmState& getState() const;

	/** Returns ItemGroup.
		@throw OmmInvalidUsageException if hasItemGroup() returns false
		@return EoaBuffer containing item group information
	*/
	const EoaBuffer& getItemGroup() const;

	/** Returns PermissionData.
		@throw OmmInvalidUsageException if hasPermissionData() returns false
		@return EoaBuffer containing permission data
	*/
	const EoaBuffer& getPermissionData() const;

	/** Returns PublisherIdUserId.
		@throw OmmInvalidUsageException if hasPublisherId() returns false
		@return publisher's user Id
	*/
	UInt32 getPublisherIdUserId() const;

	/** Returns PublisherIdUserAddress.
		@throw OmmInvalidUsageException if hasPublisherId() returns false
		@return publisher's user address
	*/
	UInt32 getPublisherIdUserAddress() const;

	/** Returns ClearCache.
		@return true if cache needs to be cleared on receipt of this status message; false otherwise
	*/
	bool getClearCache() const throw();

	/** Returns PrivateStream.
		@return true if this is private stream item
	*/
	bool getPrivateStream() const throw();

	/** Returns a buffer that in turn provides an alphanumeric null-terminated hexadecimal string representation.
		@return a buffer with the message hex information
	*/
	const EoaBuffer& getAsHex() const throw();

	/** Returns a string representation of the class instance.
		@throw OmmMemoryExhaustionException if app runs out of memory
		@return string representation of the class instance
	*/
	const EoaString& toString() const;

	/** Operator const char* overload.
	*/
	operator const char* () const;
	//@}

private:

	StatusInfo();
	virtual ~StatusInfo();

	StatusInfoImpl*			_pStatusInfoImpl;

	friend class ConsumerServiceImpl;                             

	StatusInfo( const StatusInfo& );
	StatusInfo& operator=( const StatusInfo& );
};

}

}

}

#endif // __thomsonreuters_eoa_foundation_statusinfo_h
