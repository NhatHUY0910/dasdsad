import {Button} from "react-bootstrap";

const googleLoginButton = () => {
    const handleGoogleLogin = () => {
        window.location.href = 'http://localhost:8080/oauth2/authorization/google';
    }

    return (
        <div className="d-grid gap-2">
            <Button variant="primary" size="lg" onClick={handleGoogleLogin}>
                Sign in with Google
            </Button>
        </div>
    )
}

export default googleLoginButton;