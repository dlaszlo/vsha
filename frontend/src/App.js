import React, {useContext, useState} from 'react'
import gql from "graphql-tag";
import {useMutation, useQuery, useSubscription} from "@apollo/react-hooks";
import ReactDOM from 'react-dom';
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap/dist/js/bootstrap.bundle.min'
import 'primereact/resources/themes/nova-light/theme.css'
import 'primereact/resources/primereact.min.css'
import 'primeicons/primeicons.css'
import * as serviceWorker from './serviceWorker';
import {ApolloProvider} from 'react-apollo'
import {ApolloClient} from 'apollo-client'
import {BatchHttpLink} from 'apollo-link-batch-http'
import {InMemoryCache} from 'apollo-cache-inmemory'
import {createUploadLink} from 'apollo-upload-client'
import {BrowserRouter, Switch} from 'react-router-dom'
import {ApolloLink, split} from "apollo-link";
import {SubscriptionClient} from 'subscriptions-transport-ws'
import {WebSocketLink} from 'apollo-link-ws'
import {getMainDefinition} from 'apollo-utilities'

const DEVICE_INFO_SUBSCRIPTION = gql`
    subscription SubscribeDeviceInfoUpdate($deviceId: String!) {
        subscribeDeviceInfoUpdate(deviceId: $deviceId) {
            deviceId,
            name,
            online,
            powerOn
        }
    }
`

const App = () => {
    const [powerOn, setPowerOn] = useState(false)
    const subscription = useSubscription(DEVICE_INFO_SUBSCRIPTION, {
        variables: {deviceId: "kapcsoloEloszoba"},
        onSubscriptionData: (data) => {
            const messageReceived = data.subscriptionData.data.subscribeDeviceInfoUpdate
            console.debug(JSON.stringify(messageReceived))
            setPowerOn(messageReceived.powerOn)
        }
    })
    return (
        <div className="App">
            {powerOn ? "true" : "false"}
        </div>
    );
}

export default App;
