import React, {useEffect, useState} from 'react'
import gql from "graphql-tag";
import {useMutation, useQuery} from "@apollo/react-hooks";
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap/dist/js/bootstrap.bundle.min'
import 'primereact/resources/themes/nova-light/theme.css'
import 'primereact/resources/primereact.min.css'
import 'primeicons/primeicons.css'
import './App.css'
import {InputSwitch} from 'primereact/inputswitch'
import {Navbar} from "react-bootstrap";
import {Card} from "primereact/card";

const QUERY_DEVICE = gql`
    query GetState($deviceId: String!) {
        getState(deviceId: $deviceId) {
            deviceId,
            displayOrder,
            groupName,
            mqttName,
            name,
            online,
            powerOn
        }
    }
`

const QUERY_GROUPS = gql`
    query GetGroups {
        getGroups {
            displayOrder,
            groupName,
            devices {
                deviceId,
                displayOrder,
                groupName,
                mqttName,
                name,
                online,
                powerOn
            }
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

const POWER = gql`
    mutation Toggle($deviceId: String!, $powerOn: Boolean!) {
        power(deviceId: $deviceId, powerOn: $powerOn)
    }
`

const SwitchControl = ({deviceId}) => {

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
            <div className="d-flex flex-row-reverse">
                <div className="p-2">
                    <InputSwitch checked={data.getState.powerOn} onChange={(e) => {
                        power({
                            variables: {
                                deviceId, powerOn: e.value
                            }
                        }).then()
                    }}/>
                </div>
                <div className="p-2 font-weight-bold">
                    {data.getState.name}
                </div>
            </div>
            }
        </>
    )

}

const Groups = () => {

    const {data, error, loading} = useQuery(QUERY_GROUPS, {
        fetchPolicy: "network-only",
        notifyOnNetworkStatusChange: true
    })

    return (
        <div className="d-flex align-content-around flex-wrap">
            {data && data.getGroups &&
            <>
                {data.getGroups.map((group, idx) => {
                    return (
                        <Card title={group.groupName} className="flex-fill">
                            {group.devices.map((device, idx2) => {
                                return <SwitchControl deviceId={device.deviceId}/>
                            })}
                        </Card>
                    )
                })}
            </>}
        </div>
    )
}


const App = () => {
    return (
        <div className="App">
            <Navbar bg="dark" variant="dark">
                <Navbar.Brand href="/">Home automation</Navbar.Brand>
            </Navbar>
            <div className="container-fluid">
                <Groups />
            </div>
        </div>
    )
}

export default App;
