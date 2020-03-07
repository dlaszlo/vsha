import React from 'react';
import ReactDOM from 'react-dom';
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap/dist/js/bootstrap.bundle.min'
import 'primereact/resources/themes/nova-light/theme.css'
import 'primereact/resources/primereact.min.css'
import 'primeicons/primeicons.css'
import App from './App';
import * as serviceWorker from './serviceWorker';
import {ApolloProvider} from 'react-apollo'
import {ApolloClient} from 'apollo-client'
import {BatchHttpLink} from 'apollo-link-batch-http'
import {InMemoryCache} from 'apollo-cache-inmemory'
import { createUploadLink } from 'apollo-upload-client'
import {BrowserRouter, Switch} from 'react-router-dom'
import {ApolloLink, split} from "apollo-link";
import { SubscriptionClient } from 'subscriptions-transport-ws'
import { WebSocketLink } from 'apollo-link-ws'
import { getMainDefinition } from 'apollo-utilities'

const httpLink = createUploadLink(new BatchHttpLink({ uri: '/graphql'}))

const subClient = new SubscriptionClient('ws://localhost:8080/subscriptions', {
    reconnect: true,
    connectionParams: {
    }
})

const wsLink = new WebSocketLink(subClient)

const link = split(
    // split based on operation type
    ({ query }) => {
        const { kind, operation } = getMainDefinition(query)
        return kind === 'OperationDefinition' && operation === 'subscription'
    },
    wsLink,
    httpLink
)

const client = new ApolloClient({
    link,
    cache: new InMemoryCache()
})

subClient.onConnected(() =>
    console.debug('Connected ')
)

subClient.onReconnected(() =>
    console.debug('Reconnected ')
)

subClient.onDisconnected(() =>
    console.debug('Disconnected ')
)

ReactDOM.render(
    <ApolloProvider client={client}>
        <App />
    </ApolloProvider>, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
