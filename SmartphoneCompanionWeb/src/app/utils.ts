export default class Utils {
    static cleanPhoneNumber(phoneNumber: string): string {
        return phoneNumber.replace(/[^0-9]/g, '').slice(-10);
    }
}
