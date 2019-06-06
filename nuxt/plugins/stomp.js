import SockJS from 'sockjs-client'
import Stomp from 'webstomp-client'

const options = {
  protocols: ['v10.stomp', 'v11.stomp', 'v12.stomp']
}

const stompClient = Stomp.over(new SockJS((process.env.NODE_ENV !== 'production' ? process.env.baseUrl : '') + '/torrssen'), options)
stompClient.connect()

export default stompClient
