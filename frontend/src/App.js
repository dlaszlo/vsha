import React, {useEffect, useState} from 'react'
import gql from "graphql-tag";
import {useMutation, useQuery} from "@apollo/react-hooks";
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap/dist/js/bootstrap.bundle.min'
import 'primereact/resources/themes/nova-light/theme.css'
import 'primereact/resources/primereact.min.css'
import 'primeicons/primeicons.css'
import {InputSwitch} from 'primereact/inputswitch'
import {Card} from 'primereact/card';
import {Panel} from 'primereact/panel';

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

const POWER = gql`
    mutation Toggle($deviceId: String!, $powerOn: Boolean!) {
        power(deviceId: $deviceId, powerOn: $powerOn)
    }
`

const SwitchControl = ({deviceId, onClick}) => {
    const [powerOn, setPowerOn] = useState(false)

    const {data, error, loading, subscribeToMore} = useQuery(QUERY_DEVICE, {
        fetchPolicy: "network-only",
        notifyOnNetworkStatusChange: true,
        variables: {
            deviceId
        }
    })

    const [power] = useMutation(POWER)

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
        <>
            {data && data.getState &&
            <div>
                {data.getState.name}
                <InputSwitch checked={data.getState.powerOn} onChange={(e) => {
                    power({
                        variables: {
                            deviceId, powerOn: e.value
                        }
                    }).then()
                }}/>
            </div>
            }
        </>
    )

}


const App = () => {
    return (
        <div className="App">
            <div className="container-fluid no-gutters p0">
                <div className="content-wrapper d-flex flex-column flex-md-row">
                    <Panel header="Nappali">
                        <SwitchControl deviceId="konnektorIroasztalLampa"/>
                        <SwitchControl deviceId="kapcsoloNappali"/>
                    </Panel>
                </div>
            </div>
        </div>
    )

}

export default App;
