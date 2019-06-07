import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const client = new Client({
  webSocketFactory: () => new SockJS((process.env.NODE_ENV !== 'production' ? process.env.baseUrl : '') + '/torrssen'),
  debug: function (str) {
    console.log(str)
  },
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000
})

client.onConnect = function (frame) {
  // Do something, all subscribes must be done is this callback
  // This is needed because this will be executed after a (re)connect
  console.log('Stomp (re)connected: ' + frame.headers['message'])
}

client.onStompError = function (frame) {
  // Will be invoked in case of error encountered at Broker
  // Bad login/passcode typically will cause an error
  // Complaint brokers will set `message` header with a brief message. Body may contain details.
  // Compliant brokers will terminate the connection after any error
  console.error('Broker reported error: ' + frame.headers['message'])
  console.error('Additional details: ' + frame.body)
}

client.activate()

export default {
  subscribe: (destination, callback) => client.subscribe(destination, callback),
  publish: (destination, body) => client.publish({ destination: destination, body: body }),
  activate: () => client.activate(),
  connected: () => client.connected
}
