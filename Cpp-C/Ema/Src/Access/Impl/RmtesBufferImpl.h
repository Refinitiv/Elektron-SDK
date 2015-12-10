/*|-----------------------------------------------------------------------------
 *|            This source code is provided under the Apache 2.0 license      --
 *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
 *|                See the project's LICENSE.md for details.                  --
 *|           Copyright Thomson Reuters 2015. All rights reserved.            --
 *|-----------------------------------------------------------------------------
 */

#ifndef __thomsonreuters_ema_access_RmtesBufferImpl_h
#define __thomsonreuters_ema_access_RmtesBufferImpl_h

#include "EmaBufferInt.h"
#include "EmaBufferU16Int.h"
#include "EmaStringInt.h"
#include "rtr/rsslRmtes.h"

namespace thomsonreuters {

namespace ema {

namespace access {

class RmtesBuffer;
class OmmRmtesDecoder;

class RmtesBufferImpl
{
public :

	RmtesBufferImpl();
	RmtesBufferImpl( UInt32 );
	RmtesBufferImpl( const char*, UInt32 );
	RmtesBufferImpl( const RmtesBufferImpl& );

	virtual ~RmtesBufferImpl();

	const EmaBuffer& getAsUTF8();

	const EmaBufferU16& getAsUTF16();

	const EmaString& toString();

	void apply( const RmtesBufferImpl& );

	void apply( const char* , UInt32 );

	void apply( const EmaBuffer& );

	void clear();

private :
	
	void reallocateRmtesCacheBuffer( const char* errorText ); 
	
	friend class OmmRmtesDecoder;

	RsslBuffer				_rsslBuffer;
	RsslRmtesCacheBuffer	_rsslCacheBuffer; 

	RsslBuffer				_rsslUTF8Buffer; 
	RsslU16Buffer			_rsslUTF16Buffer; 

	EmaStringInt			_toString;      
	EmaBufferInt			_utf8Buffer; 
	EmaBufferU16Int			_utf16Buffer; 

	bool					_rsslUTF8BufferSet;
	bool					_rsslUTF16BufferSet;

	bool					_applyToCache;
};

}

}

}

#endif // __thomsonreuters_ema_access_RmtesBufferImpl_h
