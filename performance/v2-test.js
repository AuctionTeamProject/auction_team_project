import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    vus: 50,
    duration: '30s',
};

export default function () {
    const res = http.get('http://localhost:8080/api/auctions/v2');

    // console.log(res.status);

    sleep(1);
}