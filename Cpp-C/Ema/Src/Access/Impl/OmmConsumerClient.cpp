/*|-----------------------------------------------------------------------------
 *|            This source code is provided under the Apache 2.0 license      --
 *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
 *|                See the project's LICENSE.md for details.                  --
 *|           Copyright Thomson Reuters 2015. All rights reserved.            --
 *|-----------------------------------------------------------------------------
 */

#include "OmmConsumerClient.h"

using namespace thomsonreuters::ema::access;

OmmConsumerClient::OmmConsumerClient()
{
}

OmmConsumerClient::~OmmConsumerClient()
{
}

void OmmConsumerClient::onRefreshMsg( const RefreshMsg&, const OmmConsumerEvent& )
{
}

void OmmConsumerClient::onUpdateMsg( const UpdateMsg&, const OmmConsumerEvent& )
{
}

void OmmConsumerClient::onStatusMsg( const StatusMsg&, const OmmConsumerEvent& )
{
}

void OmmConsumerClient::onGenericMsg( const GenericMsg&, const OmmConsumerEvent& )
{
}

void OmmConsumerClient::onAckMsg( const AckMsg&, const OmmConsumerEvent& )
{
}

void OmmConsumerClient::onAllMsg( const Msg&, const OmmConsumerEvent& )
{
}

