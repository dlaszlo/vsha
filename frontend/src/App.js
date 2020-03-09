import React, {useContext, useEffect, useState} from 'react'
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
import {BrowserRouter, Switch} from 'react-router-dom'
import {ApolloLink, split} from "apollo-link";
import {SubscriptionClient} from 'subscriptions-transport-ws'
import {WebSocketLink} from 'apollo-link-ws'
import {getMainDefinition} from 'apollo-utilities'

const QUERY_DEVICE = gql`
    query GetState($deviceId: String!) {
        getState(deviceId: $deviceId) {
            deviceId,
            name,
            online,
            powerOn
        }
    }
`

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

const UPDATE_STATE = gql`
    mutation UpdateState($deviceId: String!) {
        updateState(deviceId: $deviceId)
    }
`

const TOGGLE = gql`
    mutation Toggle($deviceId: String!) {
        toggle(deviceId: $deviceId)
    }
`

const SwitchDisplay = ({name, online, powerOn}) => {
    return (
        <div>
            {name ? name : ""} -
            {online ? "Online" : "Offline"} -
            {powerOn ? "Bekapcsolva" : "Kikapcsolva"}
        </div>
    )
}

const SwitchControl = ({deviceId, onClick}) => {
    const [powerOn, setPowerOn] = useState(false)

    const {data, error, loading, subscribeToMore} = useQuery(QUERY_DEVICE, {
        fetchPolicy: "network-only",
        notifyOnNetworkStatusChange: true,
        variables: {
            deviceId
        }
    })

    const [toggle] = useMutation(TOGGLE, {
        variables: {
            deviceId
        }
    })

    useEffect(() => {
        const unsubscribe = subscribeToMore({
            document: DEVICE_INFO_SUBSCRIPTION,
            fetchPolicy: "network-only",
            variables: {
                deviceId
            },
            updateQuery: (prev, {subscriptionData}) => {
                return {getState: subscriptionData.data.subscribeDeviceInfoUpdate}
            }
        })
        return () => unsubscribe()
    }, [])

    return (
        <div onClick={() =>
            toggle()
        }>
            {data && data.getState &&
            <SwitchDisplay name={data.getState.name} online={data.getState.online} powerOn={data.getState.powerOn}/>
            }
        </div>
    )

}


const App = () => {
    return (
        <div className="App">
            <SwitchControl deviceId="konnektorIroasztalLampa"/>
            <SwitchControl deviceId="kapcsoloNappali"/>
        </div>
    )

}

export default App;
