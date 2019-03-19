/*|-----------------------------------------------------------------------------
 *|            This source code is provided under the Apache 2.0 license      --
 *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
 *|                See the project's LICENSE.md for details.                  --
 *|           Copyright Thomson Reuters 2015. All rights reserved.            --
 *|-----------------------------------------------------------------------------
 */

#include "OmmConsumerConfig.h"
#include "OmmConsumerConfigImpl.h"

using namespace thomsonreuters::ema::access;

OmmConsumerConfig::OmmConsumerConfig() :
	_pImpl( 0 )
{
	try {
		_pImpl = new OmmConsumerConfigImpl(EmaString());
	}
	catch ( std::bad_alloc& ) {}

	if ( !_pImpl )
		throwMeeException( "Failed to allocate memory for OmmConsumerConfigImpl in OmmConsumerConfig()." );
}

OmmConsumerConfig::OmmConsumerConfig(const EmaString & path) :
	_pImpl( 0 )
{
	try {
		_pImpl = new OmmConsumerConfigImpl(path);
	}
	catch ( std::bad_alloc& ) {}

	if ( !_pImpl )
		throwMeeException( "Failed to allocate memory for OmmConsumerConfigImpl in OmmConsumerConfig()." );
}

OmmConsumerConfig::~OmmConsumerConfig()
{
	if ( _pImpl )
		delete _pImpl;
}

OmmConsumerConfig& OmmConsumerConfig::clear()
{
	_pImpl->clear();
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::username( const EmaString& username )
{
	_pImpl->username( username );
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::password( const EmaString& password )
{
	_pImpl->password( password );
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::position( const EmaString& position )
{
	_pImpl->position( position );
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::applicationId( const EmaString& applicationId )
{
	_pImpl->applicationId( applicationId );
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::host( const EmaString& host )
{
	_pImpl->host( host );
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::operationModel( OperationModel operationModel )
{
	_pImpl->operationModel( operationModel );
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::consumerName( const EmaString& consumerName )
{
	_pImpl->consumerName( consumerName );
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::tunnelingProxyHostName(const EmaString& proxyHostName)
{
	_pImpl->proxyHostName(proxyHostName);
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::tunnelingProxyPort(const EmaString& proxyPort)
{
	_pImpl->proxyPort(proxyPort);
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::tunnelingSecurityProtocol(int securityProtocol)
{
	_pImpl->securityProtocol(securityProtocol);
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::tunnelingObjectName(const EmaString& objectName)
{
	_pImpl->objectName(objectName);
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::tunnelingLibSslName(const EmaString& libsslName)
{
	_pImpl->libsslName(libsslName);
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::tunnelingLibCryptoName(const EmaString& libcryptoName)
{
	_pImpl->libcryptoName(libcryptoName);
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::config( const Data& config )
{
	_pImpl->config( config );
	return *this;
}

OmmConsumerConfig& OmmConsumerConfig::addAdminMsg( const ReqMsg& reqMsg )
{
	_pImpl->addAdminMsg( reqMsg );
	return *this;
}

OmmConsumerConfigImpl* OmmConsumerConfig::getConfigImpl() const
{
	return _pImpl;
}
